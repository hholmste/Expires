@Expires is the annotation that should not exist, but I'm making it anyway. (So suck it utopian world!)

@Expires is an annotation that performs much the same role as @Deprecated, but with two additions: First it comes with an expiry day. Before the expiry-date, using an element annotated with @Expires will yield a compilation warning. After the given date, the warning becomes a compilation error. No more can your colleagues ignore deprecation-warnings forever!

Secondly, the @Expires enforces a usage-text. When you expire something you _must_ provide a text describing what people should use instead (or an empty string, but seriously, you're not _that_ lazy, right?)

This is still quite an immature project, with several missing key-features, and currently no actual way of integrating it into your build. However, in principle, you should only have to build a jar from the main/java sources and put that jar on your classpath.

Currently, only Java 7 is supported, but I expect to support Java 6 as well.

Constructors can for some reason not expire for now, but they will at some point.

Static init and instance init are not yet supported.
