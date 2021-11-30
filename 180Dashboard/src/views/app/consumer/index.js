import React from "react";
import {Route, Switch, Redirect} from "react-router-dom";
import AppLayout from "../../../layout/AppLayout";
import Dashboard from "./Dashboard";

export default (props) => {
    const {match} = props.property;

    return (
        <AppLayout>
            <Switch>
                <Redirect exact from={`${match.url}/`} to={`${match.url}/dashboard`}/>
                <Route path={`${match.url}/dashboard`} component={Dashboard}/>
                <Redirect to="/error"/>
            </Switch>
        </AppLayout>
    );
}
