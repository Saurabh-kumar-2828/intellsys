import {Session, createSession} from "~/backend/sessions.server";
import {User, getAccountsPostgresDatabaseManager, insertUserInDatabase} from "~/backend/users.server";
import {getRequiredEnvironmentVariable} from "~/common-remix--utilities/utilities.server";
import {generateUuid, getCurrentIsoTimestamp} from "~/global-common-typescript/utilities/utilities";
import {Iso8601DateTime, Jwt, Uuid} from "~/utilities/typeDefinitions";

export function getGoogleRedirectUri() {
    const redirectBaseUri = getRequiredEnvironmentVariable("REDIRECT_BASE_URI");
    const redirectUri = `${redirectBaseUri}/v1/oauth-callback/40ee5f0a-ce1e-4439-9a35-4d6307e87f7b`;
    return redirectUri;
}

export type GoogleAccountCredentials = {
    accessToken: string;
    refreshToken: string;
};

export async function getGoogleAccessAndRefreshToken(authorizationCode: string): Promise<GoogleAccountCredentials | Error> {
    const redirectUri = getGoogleRedirectUri();
    const googleClientId = getRequiredEnvironmentVariable("GOOGLE_CLIENT_ID");
    const googleClientSecret = getRequiredEnvironmentVariable("GOOGLE_CLIENT_SECRET");

    // Post api to retrieve access token by giving authorization code.
    const url = `https://oauth2.googleapis.com/token?client_id=${googleClientId}&client_secret=${googleClientSecret}&redirect_uri=${redirectUri}&code=${authorizationCode}&grant_type=authorization_code`;

    const response = await fetch(url, {
        method: "POST",
    });
    const responseBody = await response.text();
    const responseBodyJson = JSON.parse(responseBody);

    if (responseBodyJson.refresh_token == null || responseBodyJson.access_token == null) {
        return new Error("Refresh token not found");
    }

    return {
        accessToken: responseBodyJson.access_token,
        refreshToken: responseBodyJson.refresh_token,
    };
}

export async function insertGoogleAuthenticatedUserInDatabase(googleAccountCredentials: GoogleAccountCredentials, userId: Uuid, name: string, email: string) {
    const user: User = {
        id: userId,
        name: name,
        email: email,
        googleAccountInformation: JSON.stringify({
            accessToken: googleAccountCredentials.accessToken,
            refreshToken: googleAccountCredentials.refreshToken,
        }),
        isEmailValidated: false,
        isBanned: false,
        isLocked: false,
    };

    const result = await insertUserInDatabase(user);
    if (result instanceof Error) {
        return result;
    }
}

export async function getUserMetadataFromAccessToken(accessToken: string): Promise<
    | {
          name: string;
          email: string;
      }
    | Error
> {
    const postgresDatabaseManager = await getAccountsPostgresDatabaseManager();
    if (postgresDatabaseManager instanceof Error) {
        return postgresDatabaseManager;
    }

    const result = await fetch("https://www.googleapis.com/oauth2/v1/userinfo", {
        headers: {
            Authorization: `Bearer ${accessToken}`,
        },
    });

    const response = await result.json();
    if (response.email == null) {
        return new Error("Could not find email from google");
    }

    return {
        email: response.email,
        name: response.name,
    };
}

export async function insertSessionInDatabase(sessionId: Uuid, userId: Uuid): Promise<void | Error> {
    const sessionCreatedAt = getCurrentIsoTimestamp();

    const parsedSessionCreatedAt = new Date(sessionCreatedAt);
    parsedSessionCreatedAt.setSeconds(parsedSessionCreatedAt.getSeconds() + 2592000);

    const sessionExpiresAt = parsedSessionCreatedAt.toISOString();

    const session: Session = {
        sessionId: sessionId,
        sessionCreatedAt: sessionCreatedAt,
        sessionExpiresAt: sessionExpiresAt,
        userId: userId,
        sessionLastUsed: getCurrentIsoTimestamp(),
    };

    const createSessionResult = await createSession(session);
    if (createSessionResult instanceof Error) {
        return createSessionResult;
    }
}
