import React, {useContext, useEffect, useState} from "react";
import {Link, useHistory} from "react-router-dom";
import {Store} from "../../store/store";
import {logout} from "../../store/auth/actions";

const Header = () => {
    const {state, dispatch} = useContext(Store);
    const history = useHistory();
    let user = useState();

    useEffect(() => {
        if (state.auth && state.auth.user) {
            user = state.auth.user;
        }
    }, [state.auth, user])

    const logOut = () => {
        logout(history, dispatch);
    };

    return (
        <nav className="navbar navbar-expand navbar-dark bg-dark">
            <div className="navbar-nav mr-auto">
                <li className="nav-item">
                    <Link to={"/home"} className="nav-link">
                        Home
                    </Link>
                </li>
            </div>

            {user ? (
                <div className="navbar-nav ml-auto">
                    <li className="nav-item">
                        <Link to={"/profile"} className="nav-link">
                            {user.username}
                        </Link>
                    </li>
                    <li className="nav-item">
                        <a href="# " className="nav-link" onClick={logOut}>
                            LogOut
                        </a>
                    </li>
                </div>
            ) : (
                <div className="navbar-nav ml-auto">
                    <li className="nav-item">
                        <Link to={"/login"} className="nav-link">
                            Login
                        </Link>
                    </li>
                </div>
            )}
        </nav>
    );
};

export default Header;
