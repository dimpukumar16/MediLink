const app = {
    state: {
        token: null,
        user: null,
        currentPatientUhid: null
    },
    
    init() {
        this.checkAuth();
        this.renderNav();
    },

    showToast(msg, type = 'info') {
        const container = document.getElementById('toast-container');
        if (!container) return;
        
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.innerText = msg;
        
        container.appendChild(toast);
        
        // Trigger reflow to enable CSS transition
        toast.offsetHeight;
        
        toast.classList.add('show');
        
        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => {
                toast.remove();
            }, 400);
        }, 3000);
    },
    
    // UI Routing
    showView(viewId) {
        document.querySelectorAll('.view').forEach(v => v.classList.remove('active'));
        document.querySelectorAll('.view').forEach(v => v.classList.add('hidden'));
        const el = document.getElementById(viewId);
        el.classList.remove('hidden');
        el.classList.add('active');
    },

    showLogin() {
        this.showView('view-login');
    },

    showRegister(role) {
        document.getElementById('register-role').value = role;
        
        let title = "Register";
        if (role === 'patient') title = "Patient Registration";
        if (role === 'doctor') title = "Doctor Registration";
        if (role === 'hospital') title = "Hospital Registration";
        
        document.getElementById('register-title').innerText = title;
        
        document.getElementById('reg-name-label').innerText = role === 'hospital' ? 'Hospital Name' : 'Full Name';
        
        // Toggle fields
        document.querySelectorAll('.role-field').forEach(f => f.classList.add('hidden'));
        document.querySelectorAll(`.${role}-field`).forEach(f => f.classList.remove('hidden'));
        
        this.showView('view-register');
    },
    
    goHome() {
        if (this.state.user) {
            this.showDashboard();
        } else {
            this.showView('view-landing');
        }
    },
    
    showDashboard() {
        const u = this.state.user;
        document.querySelectorAll('.user-name').forEach(el => el.innerText = u.name);
        
        if (u.role === 'PATIENT') {
            document.getElementById('patient-uhid').innerText = u.uhid;
            this.showView('view-dashboard-patient');
            this.fetchPatientRecords(u.uhid, 'patient-timeline');
        } else if (u.role === 'DOCTOR') {
            this.showView('view-dashboard-doctor');
            document.getElementById('search-uhid').value = '';
            document.getElementById('patient-details').classList.add('hidden');
            document.getElementById('doctor-patient-history').classList.add('hidden');
            document.getElementById('doc-pat-success').classList.add('hidden');
        } else if (u.role === 'HOSPITAL') {
            this.showView('view-dashboard-hospital');
            this.fetchHospitalDoctors();
        } else if (u.role === 'ADMIN') {
            this.showView('view-dashboard-admin');
            this.fetchHospitalsList();
        }
    },

    // Auth state
    checkAuth() {
        const auth = localStorage.getItem('medilink_auth');
        if (auth) {
            this.state = JSON.parse(auth);
            this.goHome();
        }
    },
    
    logout() {
        localStorage.removeItem('medilink_auth');
        this.state = { token: null, user: null };
        this.renderNav();
        this.goHome();
    },
    
    renderNav() {
        const nav = document.getElementById('nav-links');
        if (this.state.user) {
            nav.innerHTML = `
                <a href="#" onclick="app.goHome()">Dashboard</a>
                <a href="#" onclick="app.logout()">Logout (${this.state.user.name})</a>
            `;
        } else {
            nav.innerHTML = `
                <a href="#" onclick="app.goHome()">Home</a>
                <a href="#" onclick="app.showLogin()">Login</a>
            `;
        }
    },

    // API Calls
    async handleLogin(e) {
        e.preventDefault();
        const un = document.getElementById('login-username').value;
        const pw = document.getElementById('login-password').value;
        const err = document.getElementById('login-error');
        
        try {
            const res = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username: un, password: pw })
            });
            
            if (!res.ok) throw new Error("Invalid credentials");
            
            const data = await res.json();
            this.state.user = data;
            this.state.token = data.token;
            localStorage.setItem('medilink_auth', JSON.stringify(this.state));
            
            this.renderNav();
            this.goHome();
            
        } catch (error) {
            err.innerText = error.message;
        }
    },
    
    async handleRegister(e) {
        e.preventDefault();
        const role = document.getElementById('register-role').value;
        const req = {
            username: document.getElementById('reg-username').value,
            password: document.getElementById('reg-password').value,
            name: document.getElementById('reg-name').value,
        };
        
        let endpoint = '/api/auth/register/' + role;
        
        if (role === 'patient') {
            req.age = document.getElementById('reg-age').value;
            req.bloodGroup = document.getElementById('reg-blood').value;
        } else if (role === 'doctor') {
            req.specialization = document.getElementById('reg-specialization').value;
        } else if (role === 'hospital') {
            req.address = document.getElementById('reg-address').value;
        }

        const err = document.getElementById('reg-error');
        
        try {
            const res = await fetch(endpoint, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(req)
            });
            
            if (!res.ok) throw new Error("Registration failed. Username may exist.");
            
            this.showToast("Registration success! Please login.", "success");
            this.showLogin();
            
        } catch (error) {
            err.innerText = error.message;
        }
    },

    async searchPatient() {
        const uhid = document.getElementById('search-uhid').value;
        const err = document.getElementById('search-error');
        const details = document.getElementById('patient-details');
        const hist = document.getElementById('doctor-patient-history');
        
        try {
            const res = await fetch('/api/records/patient/search/' + uhid);
            if (!res.ok) throw new Error("Patient not found");
            
            const patient = await res.json();
            err.innerText = "";
            details.classList.remove('hidden');
            hist.classList.remove('hidden');
            
            document.getElementById('sp-name').innerText = patient.name;
            document.getElementById('sp-age').innerText = patient.age;
            document.getElementById('sp-blood').innerText = patient.bloodGroup;
            
            this.state.currentPatientUhid = patient.uhid;
            this.fetchPatientRecords(patient.uhid, 'doctor-timeline');
            
        } catch (error) {
            err.innerText = error.message;
            details.classList.add('hidden');
            hist.classList.add('hidden');
        }
    },

    async addRecord(e) {
        e.preventDefault();
        
        const req = {
            patientUhid: this.state.currentPatientUhid,
            doctorId: this.state.user.id,
            diagnosis: document.getElementById('record-diagnosis').value,
            prescription: document.getElementById('record-prescription').value
        };
        
        try {
            const res = await fetch('/api/records/', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(req)
            });
            
            if (!res.ok) throw new Error("Failed to add record");
            
            document.getElementById('record-diagnosis').value = '';
            document.getElementById('record-prescription').value = '';
            this.showToast("Record added successfully!", "success");
            
            this.fetchPatientRecords(this.state.currentPatientUhid, 'doctor-timeline');
            
        } catch (error) {
            this.showToast(error.message, "error");
        }
    },
    
    async fetchPatientRecords(uhid, timelineId) {
        const tl = document.getElementById(timelineId);
        try {
            const res = await fetch('/api/records/' + uhid);
            if (!res.ok) throw new Error();
            const records = await res.json();
            
            if (records.length === 0) {
                tl.innerHTML = '<p class="text-muted">No medical history found.</p>';
                return;
            }
            
            tl.innerHTML = records.map(r => `
                <div class="timeline-item">
                    <div class="timeline-date">${new Date(r.recordDate).toLocaleString()}</div>
                    <div class="timeline-content">
                        <h4>${r.diagnosis}</h4>
                        <p><strong>Prescription:</strong><br>${r.prescription.replace(/\n/g, '<br>')}</p>
                        <div class="meta">
                            Dr. ${r.doctorName} • ${r.hospitalName}
                        </div>
                    </div>
                </div>
            `).join('');
            
        } catch (error) {
            tl.innerHTML = '<p class="error-msg">Failed to load records.</p>';
        }
    },

    async fetchHospitalsList() {
        const list = document.getElementById('admin-hospitals-list');
        try {
            const res = await fetch('/api/auth/hospitals');
            const data = await res.json();
            if (data.length === 0) {
                list.innerHTML = '<p class="text-muted" style="font-size: 0.9rem; color: var(--text-muted);">No hospitals registered yet.</p>';
                return;
            }
            list.innerHTML = data.map(h => `
                <div class="timeline-item" style="padding-left: 2rem; margin-bottom: 1.5rem;">
                    <div class="timeline-content" style="padding: 1rem;">
                        <h4 style="color: #00c6ff; font-size: 1.1rem; margin-bottom: 0.25rem;">${h.name}</h4>
                        <p style="font-size: 0.9rem; margin-bottom: 0.25rem;"><strong>Address:</strong> ${h.address}</p>
                        <div class="meta" style="font-size: 0.8rem; color: var(--text-muted); border-top: 1px solid var(--surface-border); padding-top: 0.25rem; margin-top: 0.25rem;">
                            Username: ${h.user.username}
                        </div>
                    </div>
                </div>
            `).join('');
        } catch (e) {
            list.innerHTML = '<p class="error-msg">Failed to load hospitals.</p>';
        }
    },

    async adminAddHospital(e) {
        e.preventDefault();
        const un = document.getElementById('admin-hosp-username').value;
        const pw = document.getElementById('admin-hosp-password').value;
        const name = document.getElementById('admin-hosp-name').value;
        const addr = document.getElementById('admin-hosp-address').value;
        const err = document.getElementById('admin-hosp-error');
        
        try {
            const res = await fetch('/api/auth/register/hospital', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username: un, password: pw, name: name, address: addr })
            });
            if (!res.ok) {
                throw new Error("Registration failed. Username may exist.");
            }
            this.showToast("Hospital registered successfully!", "success");
            document.getElementById('admin-hosp-username').value = '';
            document.getElementById('admin-hosp-password').value = '';
            document.getElementById('admin-hosp-name').value = '';
            document.getElementById('admin-hosp-address').value = '';
            err.innerText = '';
            this.fetchHospitalsList();
        } catch (error) {
            err.innerText = error.message;
        }
    },

    async fetchHospitalDoctors() {
        const list = document.getElementById('hospital-doctors-list');
        const hospitalUserId = this.state.user.id;
        try {
            const res = await fetch(`/api/auth/hospitals/${hospitalUserId}/doctors`);
            const data = await res.json();
            if (data.length === 0) {
                list.innerHTML = '<p class="text-muted" style="font-size: 0.9rem; color: var(--text-muted);">No doctors registered yet.</p>';
                return;
            }
            list.innerHTML = data.map(d => `
                <div class="timeline-item" style="padding-left: 2rem; margin-bottom: 1.5rem;">
                    <div class="timeline-content" style="padding: 1rem;">
                        <h4 style="color: #00c6ff; font-size: 1.1rem; margin-bottom: 0.25rem;">Dr. ${d.name}</h4>
                        <p style="font-size: 0.9rem; margin-bottom: 0.25rem;"><strong>Specialization:</strong> ${d.specialization}</p>
                        <div class="meta" style="font-size: 0.8rem; color: var(--text-muted); border-top: 1px solid var(--surface-border); padding-top: 0.25rem; margin-top: 0.25rem;">
                            Username: ${d.user.username}
                        </div>
                    </div>
                </div>
            `).join('');
        } catch (e) {
            list.innerHTML = '<p class="error-msg">Failed to load doctors.</p>';
        }
    },

    async hospitalAddDoctor(e) {
        e.preventDefault();
        const un = document.getElementById('hosp-doc-username').value;
        const pw = document.getElementById('hosp-doc-password').value;
        const name = document.getElementById('hosp-doc-name').value;
        const spec = document.getElementById('hosp-doc-specialization').value;
        const err = document.getElementById('hosp-doc-error');
        const hospitalUserId = this.state.user.id;
        
        try {
            const res = await fetch('/api/auth/register/doctor', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username: un, password: pw, name: name, specialization: spec, hospitalUserId: hospitalUserId })
            });
            if (!res.ok) {
                throw new Error("Registration failed. Username may exist.");
            }
            this.showToast("Doctor registered successfully!", "success");
            document.getElementById('hosp-doc-username').value = '';
            document.getElementById('hosp-doc-password').value = '';
            document.getElementById('hosp-doc-name').value = '';
            document.getElementById('hosp-doc-specialization').value = '';
            err.innerText = '';
            this.fetchHospitalDoctors();
        } catch (error) {
            err.innerText = error.message;
        }
    },

    async doctorAddPatient(e) {
        e.preventDefault();
        const un = document.getElementById('doc-pat-username').value;
        const pw = document.getElementById('doc-pat-password').value;
        const name = document.getElementById('doc-pat-name').value;
        const age = document.getElementById('doc-pat-age').value;
        const blood = document.getElementById('doc-pat-blood').value;
        const err = document.getElementById('doc-pat-error');
        const successBox = document.getElementById('doc-pat-success');
        const successUhid = document.getElementById('doc-pat-success-uhid');
        
        try {
            const res = await fetch('/api/auth/register/patient', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username: un, password: pw, name: name, age: age, bloodGroup: blood })
            });
            if (!res.ok) {
                throw new Error("Registration failed. Username may exist.");
            }
            const patient = await res.json();
            document.getElementById('doc-pat-username').value = '';
            document.getElementById('doc-pat-password').value = '';
            document.getElementById('doc-pat-name').value = '';
            document.getElementById('doc-pat-age').value = '';
            document.getElementById('doc-pat-blood').value = '';
            err.innerText = '';
            
            // Display unique UHID
            successUhid.innerText = patient.uhid;
            successBox.classList.remove('hidden');
        } catch (error) {
            err.innerText = error.message;
            successBox.classList.add('hidden');
        }
    }
};

// Start app
document.addEventListener('DOMContentLoaded', () => {
    app.init();
    
    // Logo click goes home
    document.querySelector('.logo').addEventListener('click', () => {
        app.goHome();
    });
});
