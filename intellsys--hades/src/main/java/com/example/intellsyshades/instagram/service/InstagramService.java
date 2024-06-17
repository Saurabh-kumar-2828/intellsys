package com.example.intellsyshades.instagram.service;

import com.example.intellsyshades.common.MetaCommon;
import org.springframework.stereotype.Service;

@Service
public class InstagramService {

  private final MetaCommon metaCommon;
  public InstagramService(MetaCommon metaCommon) {
    this.metaCommon = metaCommon;
  }

  public String generateOAuthUrl(String callbackUrl, String state){
    String scope = "ads_read, ads_management, email, public_profile, business_management";
    return metaCommon.getOAuthUrl(callbackUrl,state,scope);
  }
}
