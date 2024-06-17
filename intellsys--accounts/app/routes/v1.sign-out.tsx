import type {ActionFunction} from "@remix-run/node";
import jwt from "jsonwebtoken";
import {checkIfSessionIsValid, markSessionAsDeleted} from "~/backend/sessions.server";
import {Uuid} from "~/common--type-definitions/typeDefinitions";
import {getRequiredEnvironmentVariable} from "~/common-remix--utilities/utilities.server";
import {getNonEmptyStringFromUnknown} from "~/global-common-typescript/utilities/typeValidationUtilities";

export const action: ActionFunction = async ({request}) => {
    const token = request.headers.get("Authorization");

    if (token == null) {
        return new Response(null, {status: 401});
    }

    const jwtKey = getNonEmptyStringFromUnknown(getRequiredEnvironmentVariable("JWT_SECRET"));
    const decodedToken = jwt.verify(token, jwtKey) as {sessionId: Uuid};

    if (decodedToken.sessionId == null) {
        return new Response(JSON.stringify({authorized: false, message: "No session ID found"}), {status: 400});
    }

    const isSessionValid = await checkIfSessionIsValid(decodedToken.sessionId);
    if (isSessionValid instanceof Error) {
        return isSessionValid;
    }

    if (isSessionValid == null) {
        return new Response(JSON.stringify({authorized: false}), {status: 200});
    }

    const markSessionAsDeletedResult = await markSessionAsDeleted(decodedToken.sessionId);
    if (markSessionAsDeletedResult instanceof Error) {
        return markSessionAsDeletedResult;
    }

    return new Response(null, {status: 200});
};
