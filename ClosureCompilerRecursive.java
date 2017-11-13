import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.logging.Level;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.CommandLineRunner;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;

public class ClosureCompilerRecursive {
   protected Path directory;
   protected int errorCount = 0;
   protected boolean writeFiles = true;

   protected String compile(File file) {
      Compiler compiler = new Compiler();
      // Only log actual errors (that cause failures), be quiet about warnings.
      // Without this, the tool may produce megabytes of warnings mixed in with
      // only one error.
      compiler.setLoggingLevel(Level.SEVERE);

      List<SourceFile> list = null;

      try {
          list =
         CommandLineRunner
         .getBuiltinExterns(CompilerOptions.Environment.BROWSER);
      } catch (IOException e) {
          System.out.println(e.getMessage());
          System.exit(1);
      }

      list.add(SourceFile.fromFile(file.getPath()));
      compiler.compile(new ArrayList<SourceFile>(), list, getOptions());
      errorCount += compiler.getResult().errors.length;
      return compiler.toSource();
   }

   public static void main(String[] args) throws IOException {
      ClosureCompilerRecursive ccr = new ClosureCompilerRecursive();
      setOptionsFromArgs(ccr, args);
      ccr.compileDirectory();
      System.exit(Math.min(ccr.errorCount, 127));
   }

   protected static void setOptionsFromArgs(ClosureCompilerRecursive ccr, String[] args) {
      String dir = null;
      if (args.length == 1) {
         dir = args[0];
      } else if (args.length == 2 && args[0].equals("--validate-syntax")) {
         ccr.validateSyntaxOnly();
         dir = args[1];
      } else {
         printHelp();
      }

      ccr.setDirectory(Paths.get(dir));
   }

   protected static void printHelp() {
      System.out.println("Usage: closure-compiler-recursive [--validate-syntax] /path/to/directory");
      System.exit(1);
   }

   public void compileDirectory() throws IOException {
      for(File file : getFilesFromDirectory()) {
         String compressedSource = compile(file);
         if (writeFiles) {
            writeFile(file, compressedSource);
         }
         // Without this, we've seen memory usage nearly double.
         System.gc();
      }
   }

   public void validateSyntaxOnly() {
      writeFiles = false;
   }

   public void setDirectory(Path dir) {
      directory = dir;
   }

   protected void writeFile(File file, String contents) throws IOException {
      PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file.toString())));
      writer.println(contents);
      writer.close();
   }

   protected File[] getFilesFromDirectory() throws IOException {
      File[] files = Files.find(
         directory,
         Integer.MAX_VALUE,
           (path, fileAttr) -> fileAttr.isRegularFile()
                               && path.toString().endsWith(".js"))
         .map((Path path) -> path.toFile())
         .toArray(File[]::new);
      // Sorting by biggest first reduces memory usage by ~50% depending on the
      // size difference in your files.
      Arrays.sort(files, (File f1, File f2) -> (int) (f2.length() - f1.length()));
      return files;
   }

   protected CompilerOptions getOptions() {
      CompilerOptions options = new CompilerOptions();

      options.setLanguageIn(LanguageMode.ECMASCRIPT_2015);
      options.setLanguageOut(LanguageMode.ECMASCRIPT5);

      CompilationLevel
          .SIMPLE_OPTIMIZATIONS
          .setOptionsForCompilationLevel(options);

      return options;
   }
}
