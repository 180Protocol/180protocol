import React from "react";
import Provider from "./provider";
import Consumer from "./consumer";
import {useAuthState} from "../../store/context";

const renderRoleBasedComponent = (role, props) => {
    switch (role) {
        case 'consumer':
            return <Consumer property={props} />;
        case 'provider':
            return <Provider property={props} />;
        default:
            return null
    }
}

export default (props) => {
    const userDetails = useAuthState();

    return (
        userDetails.user && userDetails.user.role ? renderRoleBasedComponent(userDetails.user.role, props) : null
    )
}
