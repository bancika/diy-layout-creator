package org.diylc.core.gerber;

import com.bancika.gerberwriter.path.Path;

public interface IGerberOutput {
  
  void write(Path path, boolean negative);
}
