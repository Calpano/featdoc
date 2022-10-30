package de.xam.featdoc.system;

import de.xam.featdoc.FeatDoc;
import de.xam.featdoc.I18n;
import de.xam.featdoc.example.CafeWiki;
import de.xam.featdoc.example.RestaurantSystemsAndScenarios;
import de.xam.featdoc.markdown.IMarkdownCustomizer;
import de.xam.featdoc.wiki.IWikiContext;

import java.io.File;
import java.io.IOException;

import static de.xam.featdoc.example.RestaurantSystemsAndScenarios.Systems.UNIVERSE;

public class GenerateExampleDocumentation {

  public static void generateFiles(Universe universe, String targetDir) throws IOException {
    final File localDir = new File("./target/"+targetDir);
    localDir.mkdirs();
    FeatDoc.generateMarkdownFiles(universe, new IWikiContext() {

      I18n i18n = new I18n(I18n.Language.en);

      @Override
      public I18n i18n() {
        return i18n;
      }


      @Override
      public IMarkdownCustomizer markdownCustomizer() {
        return new IMarkdownCustomizer() {
        };
      }

      @Override
      public File rootDir() {
        return localDir;
      }
    });
  }

}
