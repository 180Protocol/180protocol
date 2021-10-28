import React from 'react';
import {Redirect, Route} from 'react-router-dom';
import {useAuthState} from '../store/context';

const AppRoutes = ({component: Component, path, ...rest}) => {
    const userDetails = useAuthState();
    return (
        <Route
            {...rest}
            render={(props) =>
                !Boolean(userDetails.user) ? (
                    <Redirect
                        to={{pathname: "/user/login", state: {from: props.location}}}
                    />
                ) : (
                    <Component {...props} />
                )
            }
        />
    );
};

export default AppRoutes;
