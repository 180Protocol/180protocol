import React from "react";
import {Field, Form, Formik} from "formik";
import {useAuthDispatch} from "../../../store/context";
import {login} from "../../../store/auth/actions";

// Styles
import styles from './Login.module.scss';

// Images
import LoginIcon from "../../../assets/images/login_icn.svg";
import RightAngleArrow from "../../../assets/images/right_angle_arrow.svg";

const Login = (props) => {
    const dispatch = useAuthDispatch();

    const save = async (values) => {
        await login(dispatch, values);
        props.history.push("/app/rewards");
    }

    const validate = (values) => {
        let errors = {};

        if (!values.username) {
            errors.username = "Please enter your username";
        }

        if (!values.password) {
            errors.password = "Please enter your password";
        }

        return errors;
    }

    return (
        <section className={`auth-section ${styles.login}`}>
            <div className='cardContainer content-center'>
                <Formik
                    validate={validate}
                    initialValues={{
                        username: "",
                        password: "",
                    }}
                    onSubmit={save}
                >
                    {({errors, touched}) => (
                        <Form className='auth-form'>
                            <div className='auth-header text-center text-white'>
                                <img src={LoginIcon} width="60" height="auto"/>
                                <h3>Login</h3>
                            </div>
                            <div className="form-group">
                                <Field
                                    className="form-control"
                                    name="username"
                                    placeholder="Username"
                                />
                                {errors.username && touched.username && (
                                    <div className="invalid-feedback-msg">
                                        {errors.username}
                                    </div>
                                )}
                            </div>
                            <div className="form-group">
                                <Field
                                    className="form-control"
                                    name="password"
                                    type="password"
                                    placeholder="Password"
                                />
                                {errors.password && touched.password && (
                                    <div className="invalid-feedback-msg">
                                        {errors.password}
                                    </div>
                                )}
                            </div>
                            <div className="form-group formGroup-Btns">
                                <button type="submit" className="form-control">
                                    <span>Sign In</span> <img src={RightAngleArrow}/>
                                </button>
                            </div>
                        </Form>
                    )}
                </Formik>
            </div>
        </section>
    )
}

export default Login;
