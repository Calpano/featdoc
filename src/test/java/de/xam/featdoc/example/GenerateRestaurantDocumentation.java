package de.xam.featdoc.example;

import de.xam.featdoc.FeatDoc;
import de.xam.featdoc.I18n;
import de.xam.featdoc.markdown.IMarkdownCustomizer;
import de.xam.featdoc.wiki.IWikiContext;

import java.io.File;
import java.io.IOException;

import static de.xam.featdoc.example.SystemsAndScenarios.Systems.UNIVERSE;

public class GenerateRestaurantDocumentation {

  private static void generateFiles() throws IOException {
    // generate locally into git repo of smaz wiki
    FeatDoc.generateMarkdownFiles(UNIVERSE, CafeWiki.wikiContext());

    // generate locally, too, for easier debugging
    // (e.g. wiki link paths are simpler, mermaid block syntax works in IntelliJ)
    final File localDir = new File("./target/FeatDocLocal");
    localDir.mkdirs();
    FeatDoc.generateMarkdownFiles(UNIVERSE, new IWikiContext() {

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

  public static void main(String[] args) throws IOException {
    SystemsAndScenarios.defineSystems();
    // let the machine roll by using scenarios
    SystemsAndScenarios.defineScenarios();
    generateFiles();
  }


}
