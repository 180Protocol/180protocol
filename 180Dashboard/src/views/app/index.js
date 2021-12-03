import React from "react";
import Provider from "./provider";
import Consumer from "./consumer";
import {useAuthState} from "../../store/context";

const renderRoleBasedComponent = (user, props) => {
    const apiUrl = 'http://localhost:' + user.port || '3000';

    switch (user.role) {
        case 'consumer':
            return <Consumer property={props} apiUrl={apiUrl}/>;
        case 'provider':
            return <Provider property={props} apiUrl={apiUrl}/>;
        default:
            return null
    }
}

export default (props) => {
    const userDetails = useAuthState();

    return (
        userDetails.user && userDetails.user.role ? renderRoleBasedComponent(userDetails.user, props) : null
    )
}
