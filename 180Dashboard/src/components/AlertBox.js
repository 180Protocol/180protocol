import Alert from 'react-popup-alert'
import 'react-popup-alert/dist/index.css'
import {forwardRef, useImperativeHandle, useState} from "react";

const AlertBox = forwardRef((props, ref) => {
    const [alert, setAlert] = useState({
        type: '',
        text: '',
        show: false
    })

    useImperativeHandle(
        ref,
        () => ({
            showAlert(type, text) {
                setAlert({
                    type: type,
                    text: text,
                    show: true
                })
            }
        }),
    )

    const onCloseAlert = () => {
        setAlert({
            type: '',
            text: '',
            show: false
        })
    }

    return (
        <Alert
            header={''}
            btnText={'Close'}
            text={alert.text}
            type={alert.type}
            show={alert.show}
            onClosePress={onCloseAlert}
            pressCloseOnOutsideClick={true}
            showBorderBottom={false}
            alertStyles={{}}
            headerStyles={{}}
            textStyles={{
                fontSize: 24
            }}
            buttonStyles={{
                padding: '6px 20px',
                fontSize: 18,
                textTransform: 'uppercase',
                fontFamily: 'Muli Bold',
                outline: 'none',
                background: '#35607E',
                color: '#ffffff',
                borderRadius: 15,
                cursor: 'pointer',
                border: '3px solid #193E6430 !important'
            }}
        />
    )
})

export default AlertBox;