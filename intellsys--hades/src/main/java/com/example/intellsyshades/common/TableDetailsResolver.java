package com.example.intellsyshades.common;

import java.util.List;
import java.util.Map;

public interface TableDetailsResolver {
  Map<String, List<Map<String, String>>> getTableDetails(int tier);
}
