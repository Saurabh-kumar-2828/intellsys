import {ActionFunction, LoaderFunction} from "@remix-run/node";

export const action: ActionFunction = async ({request, params}) => {};

export const loader: LoaderFunction = async ({request, params}) => {
    return null;
};

export default function Home() {
    return <></>;
}
