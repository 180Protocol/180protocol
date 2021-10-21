import React from "react";
import ReactDOM from "react-dom";
import { StoreProvider } from './store/store';
import App from "./App";
import * as serviceWorker from "./serviceWorker";

ReactDOM.render(
    <StoreProvider>
        <App/>
    </StoreProvider>,
    document.getElementById("root")
);

// If you want your app to work offline and load faster, you can chađinge
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
