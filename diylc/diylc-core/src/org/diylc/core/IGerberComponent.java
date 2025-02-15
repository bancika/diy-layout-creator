package org.diylc.core;

import java.util.List;
import com.bancika.gerberwriter.DataLayer;

public interface IGerberComponent {

  void drawToGerber(DataLayer dataLayer);
  
  List<GerberLayer> getGerberLayers();
}
