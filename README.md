@Expires is an annotation that gives you more control over your deprecations. Specifically, it gives you a date after which your deprecation-warnings become compiler-errors.

If the compiler comes across a reference to an @Expires method or object-reference before the expiry-date, it will give a warning, just like @Deprecated.

If the compiler comes across a reference to an @Expires method or object-reference after the expiry-date, it will fail the compilation with a compile-error describing the preferred usage.

TODO: What happens when stuff gets initialized outside of methods? Static init? Instance init?
