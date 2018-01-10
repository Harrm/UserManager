import React from 'react'
import ReactDOM from 'react-dom'
import * as account_utils from './account.js'
import './user_manager.css'

const apiAddress = '/user-manager-1.0-SNAPSHOT/api/accounts/'

// a React component with the following properties:
// className: additional CSS classes
// onClick: standart button onClick callback
// text: text written on the button
function ControlButton(props) {
    return (<button className={"control-button "+props.className} onClick={props.onClick}>
                {props.text}
             </button>)
}

// a React component that shows a user an account provided in properties
// has a menu of three buttons: Log Out, Edit and Delete
// their behavior is specified by the client of the class through the properties
// Properties:
// account: JSON object, an account to be shown
// onBack, onEdit, onDelete: callbacks for the buttons in the top menu
// if the account is invalid or not set shows an issue instead of an account view
function AccountView(props) {
    let account = null
    if(props.hasOwnProperty("account")) {
        try {
            account_utils.validate(props.account)
            account = props.account
        } catch (e) {
            return (<p style={{color: "red"}}>{e.toString()}</p>) 
        }
    } else {
        return (<p style={{color: "red"}}>Account is not set!</p>)
    }

    return (
        <div style={{display: "table"}}>
        <div className="buttons">
            <ControlButton text="Log Out" onClick={props.onBack}/>
            <ControlButton text="Edit" className="edit-button" onClick={props.onEdit}/>
            <ControlButton text="Delete" className="delete-button" onClick={props.onDelete}/>
        </div>

        <div className="editing-grid">
            <div>Name:</div>
            <div>{account.name}</div>
            <div>Birthday:</div>
            <div>{account.birthday}</div>
            <div>Sex:</div>
            <div>{account.sex}</div>
            <div>Login:</div>
            <div>{account.login}</div>
            <div>Password:</div>
            <div>{account.password}</div>
        </div>
        </div>)
}



// a React component that allows an account editing
// has a Back button in the top menu and a Save button in a form with account fields
// Properties:
// account: JSON object, an account to be edited
// onBack: a callback for the Back button
// onSubmit: a callback for the Save button
function AccountEdit(props) {
    let account = account_utils.createDefault()
    if(props.hasOwnProperty("account")) {
        account = props.account
    }
    return (
        <div style={{display: "table"}}>
        <div className="buttons">
            <ControlButton text="Back" onClick={props.onBack} />
        </div>
        <form onSubmit={props.onSubmit}>
            <div className="editing-grid">
                <div>Name:</div>
                <input type="text" 
                       defaultValue={account.name} 
                       onChange={(event)=>{
                           account.name = event.target.value
                       }} />
                <div>Birthday:</div>
                <input type="date" 
                       defaultValue={account.birthday} 
                       onChange={(event)=>{
                            account.birthday = event.target.value
                       }} />
                <div>Sex:</div>
                <select defaultValue={account.sex} 
                        onChange={(event)=>{
                            account.sex = event.target.value
                        }}>
                    <option value="Male">Male</option>
                    <option value="Female">Female</option>
                </select>
                <div>Login:</div>
                <input type="text" defaultValue={account.login} 
                       onChange={
                           (event)=>{account.login = event.target.value
                       }} />
                <div>Password:</div>
                <input type="text" defaultValue={account.password} 
                       onChange={(event)=>{
                           account.password = event.target.value
                       }} />
            </div>
            <input className="control-button" type="submit" value="Save" />
        </form>
        </div>)
}


// a React component that allows a user to specify their login
// Properties:
// onLogin: callback called when the Login button is pressed, the only argument contains the string user has put into a text input field
class LoginDialog extends React.Component {
    constructor(props) {
        super(props)
        this.state = {login: null}
    }

    render() {
        return <div className="vertical-aligner">
               <form onSubmit={()=>{this.props.onLogin(this.state.login)}}>
                    <input type="text" 
                           onChange={(event)=>{
                               this.setState({login: event.target.value})
                           }}
                           ref={(input) => { if (input != null) input.focus(); }} />
                   <input className="control-button" type="submit" value="Login" />
               </form>
               </div>
    }
}

// a React component that manages AccountView and AccountEdit
// Properties:
// login: a login of the account to be processed
// onBack: a callback for the Back button of AccountView
// If the account is not found, notifies the user of it with a red text message instead of the account view
class Editor extends React.Component {
    constructor(props) {
        super(props)
        this.state = {editMode: false, account: 'loading', initialLogin:this.props.login}
        
        account_utils.get(this.state.initialLogin, apiAddress).then((result) => {
            this.setState({account: result, editMode: false})
        }).catch((e) => {
            this.setState({account: null})
        })

    }

    render() {
        if (!this.props.hasOwnProperty("login"))
            return (<div style={{color: "red"}}>Login is undefined!</div>)

        if (this.state.account == 'loading')
            return (<div>User is loading...</div>)

        if(this.state.account == null)
            return (<div>
                    <div style={{color: "red"}}>User {this.props.login} not found!</div>
                    <ControlButton text="Back" onClick={this.props.onBack} />
                    </div>)

        return this.state.editMode 
                ? (<AccountEdit account={this.state.account} 
                                onSubmit={this.handleSubmit.bind(this)}
                                onBack={()=>{this.setState({editMode: false})}} />)
                : (<AccountView account={this.state.account} 
                                onEdit={()=>{this.setState({editMode: true})}}
                                onBack={this.props.onBack}
                                onDelete={()=>{
                                    account_utils.remove(this.state.initialLogin, apiAddress)
                                    this.props.onBack()
                                }}/>)
        
    }

    handleSubmit(event) {
        account_utils.update(this.state.account, this.state.initialLogin, apiAddress).then(()=>{
            this.setState({editMode: false, initialLogin: this.state.account.login})
        }).catch((error) => {
            alert(error)
        })
        this.props.uponEdition(this.state.account)
        event.preventDefault()
    }
}


// a React component representing a simple menu with Log in and Sign up buttons
// which behavior is set by property callbacks onLogIn and onSignIn
class Entrance extends React.Component {
    render() {
        return (
            <div className="vertical-aligner" >
            <div className="entrance">
                <ControlButton text="Log in" onClick={this.props.onLogIn}/>
                <div style={{textAlign:"center"}}>or</div>
                <ControlButton text="Sign up" onClick={this.props.onSignIn}/>
            </div>
            </div>
        )
    }
}


// a React components that allows a user to create an account and save it on the server using AccountEdit
// Properties:
// onBack: a callback for the Back button in a top menu
// uponCreation: called after a successful conclusion of POST request, the only argument is the response of the server
// If the request results in an error, alerts of it.
class Creator extends React.Component {
    constructor(props) {
        super(props)
        this.state = {account: account_utils.createDefault()}
    }

    render() {
        return (<AccountEdit account={this.state.account} 
                             onSubmit={this.handleSubmit.bind(this)}
                             onBack={this.props.onBack} />)
    }

    handleSubmit(event) {
        account_utils.post(this.state.account, apiAddress).then((result)=>{
            this.props.uponCreation(this.state.account)

        }).catch((error) => {
            alert(error)
        })
        event.preventDefault()
    }
} 


// a React component that represents a web application that is intended to register, show, edit and delete user accounts
class UserManager extends React.Component {
    constructor(props) {
        super(props)
        this.state = {currentLogin: "", screen: Entrance}
    }

    render() {
        switch(this.state.screen) {
        case Entrance:
            return <Entrance onLogIn={()=>{this.setState({screen: LoginDialog})}} 
                        onSignIn={()=>{this.setState({screen: Creator})}}/>
        case Creator:
            return <Creator uponCreation={
                        (account)=>{
                            this.setState({screen: Editor, 
                                           currentLogin: account.login})
                        }}
                    onBack={()=>{this.setState({screen: Entrance})}} />
        case LoginDialog:
            return <LoginDialog onLogin={
                (login)=>{this.setState({screen: Editor, 
                                         currentLogin: login})
                }} />
        case Editor:
            return <Editor login={this.state.currentLogin}
                           uponEdition={
                               (account)=>{
                                    this.setState({screen: Editor, 
                                                   currentLogin: account.login})
                               }}
                           onBack={()=>{this.setState({screen: Entrance})}}/>
        
        }
    }
}



ReactDOM.render(
  <UserManager />,
  document.getElementById('root')
);
