import React from "react";
import {BrowserRouter as Router, Switch, Route, Redirect} from "react-router-dom";

import "bootstrap/dist/css/bootstrap.min.css";
import "./App.css";

import {history} from "./utils/history";
import {AuthProvider} from "./store/context";
import AppRoutes from "./components/AppRoute";
import user from "./views/user";
import app from "./views/app";
import error from "./views/error";
import main from "./views"

const App = () => {
    return (
        <AuthProvider>
            <Router history={history}>
                <Switch>
                    <AppRoutes path="/app" component={app}/>
                    <Route path="/user" component={user}/>
                    <Route path="/error" exact component={error}/>
                    <Route path="/" exact component={main}/>
                    <Redirect to="/error"/>
                </Switch>
            </Router>
        </AuthProvider>
    );
};

export default App;
