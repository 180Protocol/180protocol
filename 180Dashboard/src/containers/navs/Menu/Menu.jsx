import React from "react";
import styles from './Menu.module.scss';
import {Link} from "react-router-dom";

const Menu = () => {
    return (
        <>
            <div className={`container ${styles.headerContainer}`}>
                <ul className={styles.headerLinksUl}>
                    <li><Link className={styles.menuLink} to={"/app/rewards"}>Rewards</Link></li>
                    <li><Link className={styles.menuLink} to={"/app/data"}>Data</Link></li>
                    <li><a className={styles.menuLink} href="#">Governance</a></li>
                </ul>
            </div>
        </>
    )
}

export default Menu;
