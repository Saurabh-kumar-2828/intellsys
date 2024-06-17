import type {ActionFunction} from "@remix-run/node";
import type {JsonWebTokenError} from "jsonwebtoken";
import jwt from "jsonwebtoken";
import {checkIfSessionIsValid} from "~/backend/sessions.server";
import {getRequiredEnvironmentVariable} from "~/common-remix--utilities/utilities.server";
import {getNonEmptyStringFromUnknown} from "~/global-common-typescript/utilities/typeValidationUtilities";

export const action: ActionFunction = async ({request}) => {
    const token = request.headers.get("Authorization");

    if (token == null) {
        return new Response(null, {status: 401});
    }

    const jwtKey = getNonEmptyStringFromUnknown(getRequiredEnvironmentVariable("JWT_SECRET"));
    try {
        const decodedToken = jwt.verify(token, jwtKey);

        if (decodedToken.sessionId == null) {
            return new Response(JSON.stringify({authorized: false, message: "No session ID found"}), {status: 400});
        }

        const loggedInUserId = await checkIfSessionIsValid(decodedToken.sessionId);
        if (loggedInUserId instanceof Error) {
            return loggedInUserId;
        }

        if (loggedInUserId == null) {
            return new Response(JSON.stringify({authorized: false}), {status: 200});
        }

        return new Response(JSON.stringify({authorized: true, userId: loggedInUserId}), {status: 200});
    } catch (error) {
        const jwtError: JsonWebTokenError = error;
        throw new Response(JSON.stringify({name: jwtError.name, cause: jwtError.cause, authorized: false}), {status: 500});
    }
};
