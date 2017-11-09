import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.List;
import java.util.ArrayList;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.CommandLineRunner;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;

public class ClosureCompilerRecursive {
   protected Integer errorCount = 0;

   public String compile(Path filename) {
      Compiler compiler = new Compiler();

      List<SourceFile> list = null;

      try {
          list =
         CommandLineRunner
         .getBuiltinExterns(CompilerOptions.Environment.BROWSER);
      } catch (IOException e) {
          System.out.println(e.getMessage());
          System.exit(1);
      }

      list.add(SourceFile.fromPath(filename, UTF_8));
      compiler.compile(new ArrayList<SourceFile>(), list, getOptions());
      errorCount += compiler.getResult().errors.length;
      return compiler.toSource();
   }

   public static void main(String[] args) throws IOException {
      ClosureCompilerRecursive ccr = new ClosureCompilerRecursive();
      ccr.compileDirectory(args[0]);
      System.exit(Math.min(ccr.errorCount, 127));
   }

   protected void compileDirectory(String dir) throws IOException {
      for(Path file : getFilesFromDirectory(dir)) {
         String compressedSource = compile(file);
         writeFile(file, compressedSource);
         System.gc();
      }
   }

   protected void writeFile(Path file, String contents) throws IOException {
      PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file.toString())));
      writer.println(contents);
      writer.close();
   }

   protected Path[] getFilesFromDirectory(String dir) throws IOException {
      return Files.find(
         Paths.get(dir),
         Integer.MAX_VALUE,
           (path, fileAttr) -> fileAttr.isRegularFile())
         .toArray(Path[]::new);
   }

   protected CompilerOptions getOptions() {
      CompilerOptions options = new CompilerOptions();

      // See :
      // closure-compiler/src/com/google/javascript/jscomp/CompilerOptions.java
      // lines 2864-2896
      options.setLanguageIn(LanguageMode.ECMASCRIPT_2015);
      options.setLanguageOut(LanguageMode.ECMASCRIPT5_STRICT);

      CompilationLevel
          .SIMPLE_OPTIMIZATIONS
          .setOptionsForCompilationLevel(options);

      return options;
   }
}
