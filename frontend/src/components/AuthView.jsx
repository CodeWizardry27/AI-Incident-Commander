export default function AuthView({
    authError,
    email,
    name,
    password,
    setEmail,
    setName,
    setPassword,
    setView,
    view,
    onLogin,
    onSignup,
                                 })
{
    return (
        <div className="auth-cuntainer">
            <div className="glass-panel auth-box">
                <div className="auth-header">
                    <h1>AI Incident Commander</h1>
                    <p>Autonomous Production Outage Remediation System</p>
                </div>

                {authError && (
                    <div
                        style={{
                            color: 'var(--color-danger)',
                            fontSize: '0.85rem',
                            marginBottom: '16px',
                            background: '1px solid rgba(244, 63, 94, 0.2)',
                        }}
                        >
                        {authError}
                    </div>
                )}

                <form onSubmit={view === 'login' ? onLogin : onSignup}>
                    {view === 'signup' && (
                    <div className="form-group">
                        <label htmlFor="name">Full Name</label>
                        <input
                            id="name"
                            type="text"
                            className="form-input"
                            placeholder="Enter your name"
                            value={name}
                            onChange={(event) => setName(event.target.value)}
                            required
                        />
                    </div>
                        )}

                    <div className="form-group">
                        <label htmlFor="email">Operator Email Address</label>
                        <input
                            id="email"
                            type="email"
                            className="form-input"
                            placeholder="name@company.com"
                            value={email}
                            onChange={(event) => setEmail(event.target.value)}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="password">Security Password</label>
                        <input
                            id="password"
                            type="password"
                            className="form-input"
                            placeholder="********"
                            value={password}
                            onChange={(event) => setPassword(event.target.value)}
                            required
                        />
                    </div>

                    <button type="submit" className="btn-primary" style={{ marginTop: '10px' }}>
                        {view === 'login' ? 'Authenticate & Enter' : 'Register Operator'}
                    </button>
                </form>

                <div style={{marginTop: '20px', textAlign: 'center', fontSize: '0.85rem'}}>
                    {view === 'login' ? (
                        <span style={{color: 'var(--color-text-secondary)'}}>
                            New Operator?{' '}
                        </span>
                        onclick={() => }
                    )}
                </div>
            </div>
        </div>
    )
}