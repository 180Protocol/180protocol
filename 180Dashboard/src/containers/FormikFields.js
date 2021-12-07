import React from "react";
import Select from "react-select";

export class FormikReactSelect extends React.Component {
    handleChange = value => {
        this.props.onChange(this.props.name, value);
    };
    handleBlur = () => {
        this.props.onBlur(this.props.name, true);
    };

    render() {
        return (
            <Select
                className={`react-select ${this.props.className}`}
                classNamePrefix="react-select"
                options={this.props.options}
                isMulti={this.props.isMulti}
                onChange={this.handleChange}
                onBlur={this.handleBlur}
                value={this.props.value}
                components={this.props.components}
            />
        );
    }
}

