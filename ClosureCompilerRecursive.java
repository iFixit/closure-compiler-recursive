import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.CommandLineRunner;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;

public class ClosureCompilerRecursive {
   public String compile(String filename, CompilerOptions options) {
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

      list.add(SourceFile.fromFile(filename));
      compiler.compile(new ArrayList<SourceFile>(), list, options);
      return compiler.toSource();
   }

   public static void main(String[] args) throws IOException {
      ClosureCompilerRecursive ccr = new ClosureCompilerRecursive();
      ccr.compileDirectory(args[0]);
   }

   protected void compileDirectory(String dir) throws IOException {
      CompilerOptions options = getOptions();
      for(Path file : getFilesFromDirectory(dir)) {
         String code = compile(file.toString(), options);
         PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file.toString())));
         writer.println(code);
         writer.close();
         System.gc();
      }
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
