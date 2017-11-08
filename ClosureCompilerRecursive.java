import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.CommandLineRunner;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;

public class ClosureCompilerRecursive {
   public static String compile(String filename, CompilerOptions options) {
      Compiler compiler = new Compiler();

      List<SourceFile> list = null;

      try {
          list =
         CommandLineRunner
         .getBuiltinExterns(CompilerOptions.Environment.BROWSER);
      } catch (IOException e) {
          System.out.println("Exception raised");
      }

      list.add(SourceFile.fromFile(filename));
      compiler.compile(new ArrayList<SourceFile>(), list, options);
      return compiler.toSource();
   }

   public static void main(String[] args) throws IOException {
      CompilerOptions options = getOptions();
      for(String filename : args) {
         String code = compile(filename, options);
         PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filename + ".out")));
         writer.println(code);
         writer.close();
         System.gc();
      }
   }

   protected static CompilerOptions getOptions() {
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

