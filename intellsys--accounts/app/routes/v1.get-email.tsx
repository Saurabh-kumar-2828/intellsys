import type {ActionFunction, LoaderFunction} from "@remix-run/node";
import {getNonEmptyStringFromUnknown, safeParse} from "~/global-common-typescript/utilities/typeValidationUtilities";
import jwt from "jsonwebtoken";
import {getRequiredEnvironmentVariable} from "~/common-remix--utilities/utilities.server";
import {getEmailFromSessionId} from "~/backend/sessions.server";

export const action: ActionFunction = async ({request}) => {
    const token = safeParse(getNonEmptyStringFromUnknown, request.headers.get("Authorization"));

    if (token == null) {
        throw new Response(null, {status: 400});
    }

    const jwtKey = getNonEmptyStringFromUnknown(getRequiredEnvironmentVariable("JWT_SECRET"));
    try {
        const decodedToken = jwt.verify(token, jwtKey);

        if (decodedToken.sessionId == null) {
            return new Response(JSON.stringify({message: "Session ID not found! Error code: 317e9944-9359-403d-a3a6-11db7c2f5351"}), {status: 400});
        }

        const email = await getEmailFromSessionId(decodedToken.sessionId);
        if (email instanceof Error) {
            return email;
        }

        if (email == null) {
            return new Response(JSON.stringify({message: "Could not find email! Error code: 59176b50-8cb6-4434-b898-962e6f47c05a"}), {status: 400});
        }

        return new Response(JSON.stringify({email: email}), {status: 200});
    } catch (e) {
        throw new Error(e);
    }
};
