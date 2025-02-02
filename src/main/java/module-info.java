/**
 * @author VISTALL
 * @since 2020-10-22
 */
module consulo.internal.dotnet.asm {
    requires jakarta.annotation;
    requires org.slf4j;

    exports consulo.internal.dotnet.asm;
    exports consulo.internal.dotnet.asm.io;
    exports consulo.internal.dotnet.asm.metadata;
    exports consulo.internal.dotnet.asm.mbel;
    exports consulo.internal.dotnet.asm.parse;
    exports consulo.internal.dotnet.asm.signature;
}