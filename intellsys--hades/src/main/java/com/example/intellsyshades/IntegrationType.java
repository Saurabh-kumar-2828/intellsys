package com.example.intellsyshades;
import com.example.intellsyshades.common.TableDetailsResolver;
import com.example.intellsyshades.googleanalytics.service.GoogleAnalyticsTableDetailsResolver;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public enum IntegrationType {
    GoogleAds(UUID.fromString("800c28ce-43ea-44b8-b6fc-077f44566296"),GoogleAnalyticsTableDetailsResolver.class),
    FacebookAds(UUID.fromString("d80731db-155e-4a24-bc58-158a57edabd7"),GoogleAnalyticsTableDetailsResolver.class),
    GoogleAnalytics(UUID.fromString("cc991d2b-dc83-458e-8e8d-9b47164c735f"), GoogleAnalyticsTableDetailsResolver.class),
    Instagram(UUID.fromString("c1e7b3f9-f613-491d-b217-029dab6eca26"),GoogleAnalyticsTableDetailsResolver.class),
    Shopify(UUID.fromString("b84a3317-7296-425a-9367-02b7dd7c3116"),GoogleAnalyticsTableDetailsResolver.class),
    Youtube(UUID.fromString("3bcd4c2a-45d8-49c3-b226-25433a6e783e"),GoogleAnalyticsTableDetailsResolver.class),
    FacebookPage(UUID.fromString("a2c4d7a3-17b4-4761-80cf-1b9e5634e293"),GoogleAnalyticsTableDetailsResolver.class);

    private static final Map<UUID, IntegrationType> uuidLookup = new HashMap<>();

    static {
        for (IntegrationType type : IntegrationType.values()) {
            uuidLookup.put(type.getUuid(), type);
        }
    }

    private final UUID uuid;
  private final Class<? extends TableDetailsResolver> resolverClass;

    IntegrationType(UUID uuid, Class<? extends TableDetailsResolver> resolverClass) {
        this.uuid = uuid;
        this.resolverClass = resolverClass;
    }

    public UUID getUuid() {
        return uuid;
    }

    public static boolean isValid(UUID uuid) {
        return uuidLookup.containsKey(uuid);
    }

    public static int getThresholdDays(UUID uuid) {
      return switch (uuid.toString()) {
        case "800c28ce-43ea-44b8-b6fc-077f44566296" -> // GoogleAds
            30;
        case "cc991d2b-dc83-458e-8e8d-9b47164c735f" -> // GoogleAnalytics
            30;
        case "3bcd4c2a-45d8-49c3-b226-25433a6e783e" -> // Youtube
            30;
        case "b84a3317-7296-425a-9367-02b7dd7c3116" -> // Shopify
            30;
        case "d80731db-155e-4a24-bc58-158a57edabd7" -> // FacebookAds
            14;
        case "c1e7b3f9-f613-491d-b217-029dab6eca26" -> // Instagram
            14;
        case "a2c4d7a3-17b4-4761-80cf-1b9e5634e293" -> // FacebookPage
            14;
        default -> throw new IllegalArgumentException("Unknown UUID: " + uuid);
      };
    }

  public Class<? extends TableDetailsResolver> getResolverClass() {
    return resolverClass;
  }

    public static TableDetailsResolver getTableDetails(UUID uuid){
      for (IntegrationType type : IntegrationType.values()) {
        if (type.getUuid().equals(uuid)) {
          try{
            return type.getResolverClass().getDeclaredConstructor().newInstance();
          } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
          }
        }
      }
      throw new IllegalArgumentException("Unknown UUID: " + uuid);
    }

    @NotNull
    public static String getName(UUID uuid) {
        return uuidLookup.get(uuid).name();
    }
}
