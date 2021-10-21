import React from "react";
import {Route, Switch, Redirect} from "react-router-dom";
import UserLayout from "../../layout/UserLayout";
import Login from "./login";

export default (props) => {
    const {match} = props;

    return (
        <UserLayout>
            <Switch>
                <Redirect exact from={`${match.url}/`} to={`${match.url}/login`}/>
                <Route path={`${match.url}/login`} component={Login}/>
                <Redirect to="/error"/>
            </Switch>
        </UserLayout>
    );
}
