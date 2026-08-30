import type { InputHTMLAttributes, TextareaHTMLAttributes } from 'react';
import { componentClassNames as cls } from '@theme/components';
import './primitives.css';

interface TextInputProps extends InputHTMLAttributes<HTMLInputElement> {
  multiline?: false;
}

interface TextAreaProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  multiline: true;
}

type InputProps = TextInputProps | TextAreaProps;

export function Input(props: InputProps) {
  if (props.multiline) {
    const { multiline: _multiline, className, ...rest } = props;
    const classes = [cls.input, className ?? ''].filter(Boolean).join(' ');
    return <textarea className={classes} {...rest} />;
  }
  const { className, ...rest } = props;
  const classes = [cls.input, className ?? ''].filter(Boolean).join(' ');
  return <input className={classes} {...rest} />;
}
