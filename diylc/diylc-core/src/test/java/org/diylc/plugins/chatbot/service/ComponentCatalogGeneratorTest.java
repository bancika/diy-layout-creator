package org.diylc.plugins.chatbot.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ComponentCatalogGeneratorTest {

  private File tempDir;

  @Before
  public void setUp() throws Exception {
    tempDir = Files.createTempDirectory("diylc_catalog_test").toFile();
  }

  @After
  public void tearDown() throws Exception {
    if (tempDir != null && tempDir.exists()) {
      File[] files = tempDir.listFiles();
      if (files != null) {
        for (File f : files) {
          f.delete();
        }
      }
      tempDir.delete();
    }
  }

  @Test
  public void testGeneratorProducesValidOutput() throws Exception {
    // Run the generator
    ComponentCatalogGenerator.main(new String[] { tempDir.getAbsolutePath() });

    File fullCatalog = new File(tempDir, "catalog_full.json");
    File indexCatalog = new File(tempDir, "catalog_index.json");

    assertTrue("catalog_full.json should exist", fullCatalog.exists());
    assertTrue("catalog_index.json should exist", indexCatalog.exists());
    
    // Read contents
    String fullContent = new String(Files.readAllBytes(fullCatalog.toPath()));
    String indexContent = new String(Files.readAllBytes(indexCatalog.toPath()));
    
    assertNotNull(fullContent);
    assertNotNull(indexContent);
    
    assertTrue("Full catalog should have a version", fullContent.contains("\"version\""));
    assertTrue("Full catalog should have components array", fullContent.contains("\"components\""));
    
    assertTrue("Index catalog should have a version", indexContent.contains("\"version\""));
    assertTrue("Index catalog should have categories object", indexContent.contains("\"categories\""));
  }
}
