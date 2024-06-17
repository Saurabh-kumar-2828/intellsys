import {ActionFunction} from "@remix-run/node";
import {getNonEmptyStringFromUnknown, getStringFromUnknown, safeParse} from "~/global-common-typescript/utilities/typeValidationUtilities";
import jwt, {JsonWebTokenError} from "jsonwebtoken";
import {getRequiredEnvironmentVariable} from "~/common-remix--utilities/utilities.server";
import {checkIfSessionIsValid} from "~/backend/sessions.server";

export const action: ActionFunction = async ({request}) => {
    const token = request.headers.get("Authorization");

    if (token == null) {
        throw new Response(null, {status: 400});
    }

    const jwtKey = getNonEmptyStringFromUnknown(getRequiredEnvironmentVariable("JWT_SECRET"));
    try {
        const decodedToken = jwt.verify(token, jwtKey);

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

        return new Response(JSON.stringify({authorized: true}), {status: 200});
    } catch (error) {
        const jwtError: JsonWebTokenError = error;
        throw new Response(JSON.stringify({name: jwtError.name, cause: jwtError.cause, authorized: false}), {status: 500});
    }
};
