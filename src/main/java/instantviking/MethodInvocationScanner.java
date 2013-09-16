package instantviking;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Pair;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

class MethodInvocationScanner extends TreeScanner<MethodInvocationTree, Set<? extends Element>>
{

    private static final String ErrorMessage = "%s: %s has expired.\nUsage: %s\n";
    private static final String WarningMessage = "%s: %s is deprecated.\nUsage: %s\n";
    private final Messager messager;
    private final TypeElement targetAnnotationType;
    private final Element invokingElement;
    private final Trees trees;
    MethodInvocationTree node;

    MethodInvocationScanner(Messager messager, TypeElement targetAnnotationType, Element invokingElement, Trees trees)
    {
        this.messager = messager;
        this.targetAnnotationType = targetAnnotationType;
        this.invokingElement = invokingElement;
        this.trees = trees;
    }

    @Override
    public MethodInvocationTree visitMethodInvocation(MethodInvocationTree node, Set<? extends Element> expiredSymbols)
    {
        this.node = node;
        JCTree.JCFieldAccess access = (JCTree.JCFieldAccess) node.getMethodSelect();
        for (Problem problem : findExpiredReferences(expiredSymbols, access))
        {
            if (problem != null)
            {
                handleExpiry(problem, node);
            }
        }
        return super.visitMethodInvocation(node, expiredSymbols);
    }

    private java.util.List<Problem> findExpiredReferences(
            Set<? extends Element> expiredSymbols,
            JCTree.JCFieldAccess referencingStatement)
    {
        java.util.List<Problem> problems = new ArrayList<>();
        for (Element expiredSymbol : expiredSymbols)
        {
            Name expiredName = expiredSymbol.getSimpleName();
            if (expiredName.equals(referencingStatement.name))
            {
                // the call is to an expired method
                problems.add(new Problem((Symbol) expiredSymbol, referencingStatement));
            }

            if (referencingStatement.selected instanceof JCTree.JCIdent &&
                    expiredName.equals(((JCTree.JCIdent) referencingStatement.selected).name))
            {
                // the call is on an expired class
                problems.add(new Problem((Symbol) expiredSymbol, referencingStatement));
            }
        }

        return problems;
    }

    private void handleExpiry(Problem problem, MethodInvocationTree methodInvocationTree)
    {
        DateAndUsage dateAndUsage = retrieveExpiryDate(problem.expiringReference);
        Date expiryDate = dateAndUsage.date;
        Date now = new Date();

        String message;
        Diagnostic.Kind messageKind;
        String location = buildLocation(
                trees.getPath(invokingElement).getCompilationUnit(),
                trees.getSourcePositions(),
                methodInvocationTree);

        String methodDescription = String.format("%s.%s", problem.referencingStatement.selected, problem.referencingStatement.name);
        if (expiryDate.before(now))
        {
            messageKind = Diagnostic.Kind.ERROR;
            message = String.format(
                    ErrorMessage,
                    location,
                    methodDescription,
                    dateAndUsage.usage);
        } else
        {
            messageKind = Diagnostic.Kind.WARNING;
            message = String.format(
                    WarningMessage,
                    location,
                    methodDescription,
                    dateAndUsage.usage);
        }

        this.messager.printMessage(messageKind, message);
    }

    private static String buildLocation(CompilationUnitTree compilationUnit, SourcePositions sourcePositions, MethodInvocationTree methodInvocationTree)
    {
        long start = sourcePositions.getStartPosition(compilationUnit, methodInvocationTree);
        long end = sourcePositions.getEndPosition(compilationUnit, methodInvocationTree);
        try
        {
            return String.format("%s [%s, line %d, column %d]",
                    compilationUnit.getSourceFile().getCharContent(true).subSequence((int) start, (int) end),
                    compilationUnit.getSourceFile().getName(),
                    compilationUnit.getLineMap().getLineNumber(start),
                    compilationUnit.getLineMap().getColumnNumber(start));
        } catch (Exception e)
        {
            return "Unknown source";
        }
    }

    private DateAndUsage retrieveExpiryDate(Symbol expiredSymbol)
    {
        for (Attribute.Compound c : expiredSymbol.getAnnotationMirrors())
        {
            if (targetAnnotationType.equals(c.getAnnotationType().asElement()))
            {
                return new DateAndUsage(buildDate(c.values), strVal(c.values));
            }
        }
        return null;
    }

    private Date buildDate(List<Pair<Symbol.MethodSymbol, Attribute>> values)
    {
        Calendar expiryDate = Calendar.getInstance();
        expiryDate.set(Calendar.DAY_OF_MONTH, intVal(values, "day"));
        expiryDate.set(Calendar.MONTH, intVal(values, "month"));
        expiryDate.set(Calendar.YEAR, intVal(values, "year"));
        return expiryDate.getTime();
    }

    private int intVal(List<Pair<Symbol.MethodSymbol, Attribute>> values, String name)
    {
        for (Pair<Symbol.MethodSymbol, Attribute> p : values)
        {
            if (name.equals(p.fst.getSimpleName().toString()))
            {
                return (int) p.snd.getValue();
            }
        }
        return 0;
    }

    private String strVal(List<Pair<Symbol.MethodSymbol, Attribute>> values)
    {
        for (Pair<Symbol.MethodSymbol, Attribute> p : values)
        {
            if ("usage".equals(p.fst.getSimpleName().toString()))
            {
                return (String) p.snd.getValue();
            }
        }
        return "I have no usage, which means the person writing the @Expiry didn't care about his or her handicraft.";
    }

    private static class DateAndUsage
    {
        public final Date date;
        public final String usage;

        private DateAndUsage(Date date, String usage)
        {
            this.date = date;
            this.usage = usage;
        }
    }

    private static class Problem
    {
        Symbol expiringReference; // the thing that is annotated with Expires
        JCTree.JCFieldAccess referencingStatement; // the statement referencing the expiring thing.

        public Problem(Symbol expiringReference, JCTree.JCFieldAccess referencingStatement)
        {
            this.expiringReference = expiringReference;
            this.referencingStatement = referencingStatement;
        }
    }
}
