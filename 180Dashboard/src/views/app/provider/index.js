import React from "react";
import {Route, Switch, Redirect} from "react-router-dom";
import AppLayout from "../../../layout/AppLayout";
import Data from "./Data";
import Rewards from "./Rewards";

export default (props) => {
    const {match} = props.property;

    return (
        <AppLayout>
            <Switch>
                <Redirect exact from={`${match.url}/`} to={`${match.url}/rewards`}/>
                <Route path={`${match.url}/rewards`} render={() => <Rewards apiUrl={props.apiUrl}/>}/>
                <Route path={`${match.url}/data`} render={() => <Data apiUrl={props.apiUrl}/>}/>
                <Redirect to="/error"/>
            </Switch>
        </AppLayout>
    );
}
