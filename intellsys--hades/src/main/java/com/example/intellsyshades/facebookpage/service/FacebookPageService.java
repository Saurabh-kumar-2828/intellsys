package com.example.intellsyshades.facebookpage.service;

import com.example.intellsyshades.common.MetaCommon;
import org.springframework.stereotype.Service;

@Service
public class FacebookPageService {

  private final MetaCommon metaCommon;
  public FacebookPageService(MetaCommon metaCommon) {
    this.metaCommon = metaCommon;
  }

  public String generateOAuthUrl(String callbackUrl, String state){
    String scope = "pages_show_list,pages_read_engagement,read_insights,pages_manage_metadata,pages_read_user_content";
    return metaCommon.getOAuthUrl(callbackUrl,state,scope);
  }
}
