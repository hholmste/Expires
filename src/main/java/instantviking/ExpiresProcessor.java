package instantviking;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import java.util.Set;

@SupportedAnnotationTypes("instantviking.Expires")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ExpiresProcessor extends AbstractProcessor
{

    private Trees trees;
    private TypeElement targetAnnotationType;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv)
    {
        super.init(processingEnv);
        trees = Trees.instance(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        if (annotations.isEmpty())
        {
            return false;
        }

        targetAnnotationType = annotations.iterator().next();
        Set<? extends Element> expiredElements = roundEnv.getElementsAnnotatedWith(Expires.class);
        for (Element rootElement : roundEnv.getRootElements())
        {
            processOneElement(rootElement, expiredElements);
        }
        return false;
    }

    private void processOneElement(Element el, Set<? extends Element> expiredElements)
    {
        for (Element sub : el.getEnclosedElements())
        {
            if (sub.getKind() == ElementKind.METHOD)
            {
                processMethod(sub, expiredElements);
            } else if (sub.getKind() == ElementKind.CONSTRUCTOR)
            {
                //TODO: Support constructors somehow
                processOneElement(sub, expiredElements);
            } else
            {
                processOneElement(sub, expiredElements);
            }
        }
    }

    private void processMethod(Element el, Set<? extends Element> expiredElements)
    {
        TreePath methodPath = trees.getPath(el);
        Tree body = methodPath.getLeaf();

        MethodInvocationScanner scanner = new MethodInvocationScanner(this.processingEnv.getMessager(), targetAnnotationType, el, trees);
        body.accept(scanner, expiredElements);
    }

}
