import React from "react";
import {Router, Switch, Route, Redirect} from "react-router-dom";

import "bootstrap/dist/css/bootstrap.min.css";
import "./App.css";

import {history} from "./utils/history";
import user from "./views/user";
import app from "./views/app";
import main from "./views";
import error from "./views/error";

const AuthRoute = ({component: Component, authUser, ...rest}) => (
    <Route
        {...rest}
        render={(props) =>
            authUser ? (
                <Component {...props} />
            ) : (
                <Redirect
                    to={{
                        pathname: "/user/login",
                        state: {from: props.location},
                    }}
                />
            )
        }
    />
);

const App = () => {
    let loginUser = localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')) : null;

    return (
        <Router history={history}>
            <Switch>
                <AuthRoute path="/app" authUser={loginUser} component={app}/>
                <Route path="/user" component={user}/>
                <Route path="/error" exact component={error}/>
                <Route path="/" exact component={main}/>
                <Redirect to="/error"/>
            </Switch>
        </Router>
    );
};

export default App;
