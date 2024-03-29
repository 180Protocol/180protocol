import React from "react";
import styles from './Header.module.scss';
import {useAuthDispatch, useAuthState} from "../../../store/context";
import {logout} from "../../../store/auth/actions";

// Images
import logo from "../../../assets/images/logo.png";
import notificationIcon from "../../../assets/images/notification.svg";
import userIcon from "../../../assets/images/user.svg";
import logoutIcon from "../../../assets/images/logout_active.svg";

const Header = () => {
    const dispatch = useAuthDispatch();
    const userDetails = useAuthState();

    const handleLogout = async () => {
        await logout(dispatch);
    };

    return (
        <>
            <header className={`${styles.header} bg-default`}>
                <div className={`container-fluid ${styles.headerContainer}`}>
                    <nav className="navbar navbar-light justify-content-between">
                        <a className="navbar-brand" href='#'>
                            <img src={logo} alt='logo' className={styles.logo}/>
                        </a>
                        {
                            userDetails.user ? <div className={styles.headerRightBar}>
                                <h3>{userDetails.user.role.toUpperCase()} Coalition Manager</h3>
                                <ul className={`${styles.navbarNav} navbar-nav ml-auto`}>
                                    <li className="nav-item active">
                                        <a className="nav-link" href="#">
                                            <div className={styles.notificationIconClass}>
                                                <img alt="Notification Icon" src={notificationIcon} width={25}/>
                                            </div>
                                        </a>
                                    </li>
                                    <li className="nav-item">
                                        <a className="nav-link" href="#">
                                            <img alt="User Icon" src={userIcon} width={25}/>
                                        </a>
                                    </li>
                                    <li className="nav-item">
                                        <a href="# " className="nav-link" onClick={handleLogout}>
                                            <img alt="Logout Icon" src={logoutIcon} width={25}/>
                                        </a>
                                    </li>
                                </ul>
                            </div> : null
                        }
                    </nav>
                </div>
            </header>
        </>
    )
}

export default Header;
