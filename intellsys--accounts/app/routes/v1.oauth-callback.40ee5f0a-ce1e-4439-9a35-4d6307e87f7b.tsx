import {LoaderFunction, json, redirect} from "@remix-run/node";
import {getGoogleAccessAndRefreshToken, getUserMetadataFromAccessToken, insertGoogleAuthenticatedUserInDatabase, insertSessionInDatabase} from "~/backend/google-oauth.server";
import {getNonEmptyStringFromUnknown, safeParse} from "~/global-common-typescript/utilities/typeValidationUtilities";
import {generateUuid} from "~/global-common-typescript/utilities/utilities";
import jwt from "jsonwebtoken";
import {getRequiredEnvironmentVariable} from "~/common-remix--utilities/utilities.server";
import Cryptr from "cryptr";
import {checkIfUserExists as getExistingUserId} from "~/backend/users.server";
import {Session, createSession, getExistingSessionId, getUserAndSessionIdForExistingUser} from "~/backend/sessions.server";
import {Uuid} from "~/common--type-definitions/typeDefinitions";
import {createEncryptionKeyForHost, getEncryptionKeyForHost} from "~/backend/encryption-keys.server";

export const loader: LoaderFunction = async ({request, params}) => {
    const urlSearchParams = new URL(request.url).searchParams;
    const authorizationCode = safeParse(getNonEmptyStringFromUnknown, urlSearchParams.get("code"));
    const callbackUrl = safeParse(getNonEmptyStringFromUnknown, urlSearchParams.get("state"));
    if (callbackUrl == null) {
        throw new Response("Callback URL cannot be null", {status: 404});
    }
    const callbackUrlParsed = new URL(callbackUrl);
    if (callbackUrlParsed instanceof Error) {
        throw new Response("Malformed callback URL", {status: 404});
    }

    const host = callbackUrlParsed.host;

    let encryptionKey = await getEncryptionKeyForHost(host);
    if (encryptionKey instanceof Error) {
        throw new Response(null, {status: 400});
    }

    if (encryptionKey == null) {
        encryptionKey = await createEncryptionKeyForHost(host);
        if (encryptionKey instanceof Error) {
            throw encryptionKey;
        }
    }

    const cryptrCommon = new Cryptr(getNonEmptyStringFromUnknown(getRequiredEnvironmentVariable("ENCRYPTION_KEYS_ENCRYPTION_KEY")));
    const decryptedEncryptionKey = cryptrCommon.decrypt(encryptionKey);

    const cryptrDomainLevel = new Cryptr(decryptedEncryptionKey);

    if (callbackUrl == null) {
        throw new Response(null, {status: 400});
    }
    if (authorizationCode == null) {
        throw new Response(null, {status: 400});
    }

    const tokenResult = await getGoogleAccessAndRefreshToken(authorizationCode);
    if (tokenResult instanceof Error) {
        throw tokenResult;
    }

    const {accessToken} = tokenResult;
    const userMetadataResult = await getUserMetadataFromAccessToken(accessToken);
    if (userMetadataResult instanceof Error) {
        throw userMetadataResult;
    }

    const {name, email} = userMetadataResult;

    const existingUserId = await getExistingUserId(email);
    if (existingUserId instanceof Error) {
        throw existingUserId;
    }

    let userId = existingUserId;

    if (userId == null) {
        userId = generateUuid();
        const insertResult = await insertGoogleAuthenticatedUserInDatabase(tokenResult, userId, name, email);
        if (insertResult instanceof Error) {
            throw insertResult;
        }
    }

    const existingSessionId = await getExistingSessionId(email);
    if (existingSessionId instanceof Error) {
        throw existingSessionId;
    }

    let sessionId = existingSessionId;

    if (sessionId == null) {
        sessionId = generateUuid();
        const createSessionResult = await insertSessionInDatabase(sessionId, userId);
        if (createSessionResult instanceof Error) {
            return createSession;
        }
    }

    const jwtKey = getRequiredEnvironmentVariable("JWT_SECRET");

    const authToken = jwt.sign(
        {
            sessionId: sessionId,
        },
        jwtKey,
    );

    const encryptedAuthToken = cryptrDomainLevel.encrypt(authToken);

    return redirect(`${callbackUrl}?token=${encryptedAuthToken}`);
};

export default function OAuthCallback() {
    return <></>;
}
