import type {ActionFunction, LoaderFunction} from "@remix-run/node";
import {json, redirect} from "@remix-run/node";
import {Form, useActionData, useLoaderData} from "@remix-run/react";
import {useEffect, useRef, useState} from "react";
import {Google} from "react-bootstrap-icons";
import {getGoogleRedirectUri} from "~/backend/google-oauth.server";
import {getRequiredEnvironmentVariable} from "~/common-remix--utilities/utilities.server";
import {HiddenFormField} from "~/global-common-typescript/components/hiddenFormField";
import {getNonEmptyStringFromUnknown, getStringFromUnknown, safeParse} from "~/global-common-typescript/utilities/typeValidationUtilities";

type LoaderData = {
    googleOAuthUrl: string;
};

export const action: ActionFunction = async ({request, params}) => {
    const body = await request.formData();

    const callbackUrl = safeParse(getStringFromUnknown, body.get("callbackUrl"));
    const redirectUri = getGoogleRedirectUri();
    const clientId = getRequiredEnvironmentVariable("GOOGLE_CLIENT_ID");
    const scope = "https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile";

    const url = `https://accounts.google.com/o/oauth2/v2/auth?scope=${scope}&client_id=${clientId}&response_type=code&redirect_uri=${redirectUri}&prompt=consent&access_type=offline&state=${callbackUrl}`;

    const actionData = {
        url: url,
    };
    return json(actionData);
    // return redirect(url);
};

export const loader: LoaderFunction = async ({request}) => {
    const redirectUri = getGoogleRedirectUri();
    const clientId = getRequiredEnvironmentVariable("GOOGLE_CLIENT_ID");
    const scope = "https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile";

    const googleOAuthUrl = `https://accounts.google.com/o/oauth2/v2/auth?scope=${scope}&client_id=${clientId}&response_type=code&redirect_uri=${redirectUri}&prompt=consent&access_type=offline`;

    const loaderData: LoaderData = {
        googleOAuthUrl: googleOAuthUrl,
    };

    return json(loaderData);
};

export default function SignUp() {
    const {googleOAuthUrl} = useLoaderData() as LoaderData;
    // const actionData = useActionData() as ActionData;

    const [callbackUrl, setCallbackUrl] = useState<string | null>(null);

    useEffect(() => {
        if (document != undefined) {
            const urlSearchParams = new URL(document.location.href).searchParams;

            const callbackUrlParsed = safeParse(getNonEmptyStringFromUnknown, urlSearchParams.get("callbackUrl"));
            if (callbackUrlParsed != null) {
                setCallbackUrl(callbackUrlParsed);
            }
        }
    }, []);

    return (
        <>
            <div className="tw-w-full tw-h-full tw-grid tw-place-items-center">
                <div className="tw-w-fit tw-h-fit tw-grid tw-place-items-center tw-grid-flow-col tw-gap-x-2 tw-bg-gray-700 tw-px-6 tw-py-3 tw-rounded-lg">
                    <Google />
                    <a
                        href={`${googleOAuthUrl}&state=${callbackUrl}`}
                        target="_blank"
                        rel="noreferrer"
                        type="submit"
                    >
                        Sign in with google
                    </a>
                </div>

                {/* <a
                    className="tw-hidden"
                    href={googleOAuthUrl}
                    target="_blank"
                    rel="noreferrer"
                >
                    Test
                </a> */}
            </div>
        </>
    );
}
