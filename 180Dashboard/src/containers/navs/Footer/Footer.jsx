import React from "react";
import styles from './Footer.module.scss';

const Footer = () => {
    let now = new Date();

    return (
        <>
            <footer>
                <div className="row">
                    <div className="col-12">
                        <div className={styles.footerText}>
                            <p>Copyright © {now.getFullYear()} <a href="#">Bond180</a>. All Rights Reserved.</p>
                            <p><a href="#">Privacy Policy</a> · <a href="#">Terms of Use</a> · <a href="#">Sitemap</a>
                            </p>
                        </div>
                    </div>
                </div>
            </footer>
        </>
    )
}

export default Footer;
