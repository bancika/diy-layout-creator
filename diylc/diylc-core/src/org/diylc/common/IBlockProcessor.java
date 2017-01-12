package org.diylc.common;

public interface IBlockProcessor {

  public static final String BLOCKS_KEY = "buildingBlocks";

  void saveSelectionAsBlock(String blockName);

  void loadBlock(String blockName) throws InvalidBlockException;
  
  void deleteBlock(String blockName);

  public class BlockAlreadyExistsException extends Exception {

    private static final long serialVersionUID = 1L;

  }

  public class InvalidBlockException extends Exception {

    private static final long serialVersionUID = 1L;

  }
}
