/**
 * Student Event-Management System - Vanilla JavaScript Engine
 * Modules: Navigation, Student CRUD, Event CRUD, Dashboard Analytics, Validations, Modals, Toasts
 */

document.addEventListener('DOMContentLoaded', () => {
  // =========================================================================
  // State Management
  // =========================================================================
  const INITIAL_STUDENTS = [
    { id: 1, studentName: 'Alex Morgan', registerNumber: '21CS001', phoneNumber: '9876543210', email: 'alex.morgan@college.edu' },
    { id: 2, studentName: 'Sophia Chen', registerNumber: '21CS002', phoneNumber: '9123456789', email: 'sophia.chen@college.edu' },
    { id: 3, studentName: 'Ethan Hunt', registerNumber: '21EC015', phoneNumber: '9988776655', email: 'ethan.hunt@college.edu' },
    { id: 4, studentName: 'Maya Lin', registerNumber: '22IT042', phoneNumber: '9543210987', email: 'maya.lin@college.edu' },
    { id: 5, studentName: 'David Miller', registerNumber: '22ME009', phoneNumber: '9012345678', email: 'david.miller@college.edu' }
  ];

  const INITIAL_EVENTS = [
    { id: 1, studentName: 'Alex Morgan', studentRegisterNumber: '21CS001', eventName: 'National Hackathon 2026', eventDate: '2026-03-15', eventPlace: 'Auditorium Hall A', prizeWon: '1st Prize' },
    { id: 2, studentName: 'Sophia Chen', studentRegisterNumber: '21CS002', eventName: 'Web Crafting Symposium', eventDate: '2026-04-10', eventPlace: 'Tech Park Lab 3', prizeWon: '2nd Prize' },
    { id: 3, studentName: 'Ethan Hunt', studentRegisterNumber: '21EC015', eventName: 'RoboWars Championship', eventDate: '2026-05-02', eventPlace: 'Indoor Sports Complex', prizeWon: '1st Prize' },
    { id: 4, studentName: 'Maya Lin', studentRegisterNumber: '22IT042', eventName: 'CodeSprint 2.0', eventDate: '2026-05-20', eventPlace: 'Main Computer Lab', prizeWon: '3rd Prize' },
    { id: 5, studentName: 'David Miller', studentRegisterNumber: '22ME009', eventName: 'CAD Design Challenge', eventDate: '2026-06-11', eventPlace: 'Mechanical Workshop B', prizeWon: 'Participation' }
  ];

  // Initialize Storage
  if (!localStorage.getItem('sem_students')) {
    localStorage.setItem('sem_students', JSON.stringify(INITIAL_STUDENTS));
  }
  if (!localStorage.getItem('sem_events')) {
    localStorage.setItem('sem_events', JSON.stringify(INITIAL_EVENTS));
  }

  let students = JSON.parse(localStorage.getItem('sem_students'));
  let events = JSON.parse(localStorage.getItem('sem_events'));

  let currentDeleteTarget = null; // { type: 'student'|'event', id: number }

  // API Backend Sync Attempt
  fetchStatsFromBackend();

  // =========================================================================
  // DOM Element Selections
  // =========================================================================
  const sidebar = document.getElementById('sidebar');
  const toggleBtn = document.getElementById('toggleSidebar');
  const navLinks = document.querySelectorAll('.nav-menu a');
  const viewSections = document.querySelectorAll('.view-section');
  const pageTitle = document.getElementById('pageTitle');

  // Modals
  const studentModal = document.getElementById('studentModal');
  const eventModal = document.getElementById('eventModal');
  const deleteModal = document.getElementById('deleteModal');

  // Forms
  const studentForm = document.getElementById('studentForm');
  const eventForm = document.getElementById('eventForm');

  // Tables
  const studentsTableBody = document.getElementById('studentsTableBody');
  const eventsTableBody = document.getElementById('eventsTableBody');

  // Inputs
  const searchStudentInput = document.getElementById('searchStudentInput');
  const searchEventInput = document.getElementById('searchEventInput');
  const eventRegSelect = document.getElementById('eventStudentReg');
  const eventStudentNameInput = document.getElementById('eventStudentName');

  // Dashboard Stats Elements
  const statTotalStudents = document.getElementById('statTotalStudents');
  const statTotalEvents = document.getElementById('statTotalEvents');
  const statFirstPrize = document.getElementById('statFirstPrize');
  const statSecondPrize = document.getElementById('statSecondPrize');
  const statThirdPrize = document.getElementById('statThirdPrize');
  const statParticipation = document.getElementById('statParticipation');

  // =========================================================================
  // Navigation & Routing
  // =========================================================================
  toggleBtn.addEventListener('click', () => {
    sidebar.classList.toggle('open');
  });

  navLinks.forEach(link => {
    link.addEventListener('click', (e) => {
      e.preventDefault();
      const targetView = link.getAttribute('data-view');
      
      navLinks.forEach(l => l.parentElement.classList.remove('active'));
      link.parentElement.classList.add('active');

      viewSections.forEach(section => {
        section.classList.remove('active');
        if (section.id === targetView) {
          section.classList.add('active');
        }
      });

      // Update Header Title
      const titleMap = {
        'dashboard-view': 'Dashboard Overview',
        'students-view': 'Student Details Management',
        'events-view': 'Event Details & Prize Tracker',
        'about-view': 'About System',
        'contact-view': 'Support & Contact'
      };
      pageTitle.textContent = titleMap[targetView] || 'Student Event Management';

      if (window.innerWidth <= 900) {
        sidebar.classList.remove('open');
      }
    });
  });

  // =========================================================================
  // Data Rendering & Dashboard Metrics
  // =========================================================================
  function renderAll() {
    renderDashboardStats();
    renderStudentsTable(students);
    renderEventsTable(events);
    populateStudentSelect();
  }

  function renderDashboardStats() {
    const totalStudents = students.length;
    const totalEvents = events.length;
    const firstPrize = events.filter(e => e.prizeWon === '1st Prize').length;
    const secondPrize = events.filter(e => e.prizeWon === '2nd Prize').length;
    const thirdPrize = events.filter(e => e.prizeWon === '3rd Prize').length;
    const participation = events.filter(e => e.prizeWon === 'Participation').length;

    statTotalStudents.textContent = totalStudents;
    statTotalEvents.textContent = totalEvents;
    statFirstPrize.textContent = firstPrize;
    statSecondPrize.textContent = secondPrize;
    statThirdPrize.textContent = thirdPrize;
    statParticipation.textContent = participation;
  }

  function saveState() {
    localStorage.setItem('sem_students', JSON.stringify(students));
    localStorage.setItem('sem_events', JSON.stringify(events));
    renderAll();
  }

  // =========================================================================
  // Student Module Operations
  // =========================================================================
  function renderStudentsTable(dataList) {
    if (dataList.length === 0) {
      studentsTableBody.innerHTML = `
        <tr>
          <td colspan="6" class="empty-state">
            <div class="empty-state-icon">👤</div>
            <p>No student records found</p>
          </td>
        </tr>
      `;
      return;
    }

    studentsTableBody.innerHTML = dataList.map((s, idx) => `
      <tr>
        <td><strong>#${idx + 1}</strong></td>
        <td>${escapeHtml(s.studentName)}</td>
        <td><code style="background:#eff6ff; padding:2px 6px; border-radius:4px; color:#1d4ed8; font-weight:600">${escapeHtml(s.registerNumber)}</code></td>
        <td>${escapeHtml(s.phoneNumber)}</td>
        <td>${escapeHtml(s.email)}</td>
        <td>
          <div class="action-btns">
            <button class="btn-icon edit" onclick="openEditStudentModal(${s.id})" title="Edit Student">✏️</button>
            <button class="btn-icon delete" onclick="confirmDeleteStudent(${s.id})" title="Delete Student">🗑️</button>
          </div>
        </td>
      </tr>
    `).join('');
  }

  // Populate Dropdown in Event Form with Registered Students
  function populateStudentSelect() {
    eventRegSelect.innerHTML = '<option value="">-- Select Student Register Number --</option>' +
      students.map(s => `<option value="${escapeHtml(s.registerNumber)}">${escapeHtml(s.registerNumber)} - ${escapeHtml(s.studentName)}</option>`).join('');
  }

  eventRegSelect.addEventListener('change', () => {
    const reg = eventRegSelect.value;
    const found = students.find(s => s.registerNumber === reg);
    if (found) {
      eventStudentNameInput.value = found.studentName;
    } else {
      eventStudentNameInput.value = '';
    }
  });

  // Search Students
  searchStudentInput.addEventListener('input', (e) => {
    const q = e.target.value.toLowerCase().trim();
    const filtered = students.filter(s =>
      s.studentName.toLowerCase().includes(q) ||
      s.registerNumber.toLowerCase().includes(q) ||
      s.email.toLowerCase().includes(q) ||
      s.phoneNumber.includes(q)
    );
    renderStudentsTable(filtered);
  });

  // Add / Edit Student Form Submission with Validations
  studentForm.addEventListener('submit', (e) => {
    e.preventDefault();
    
    // Clear error states
    clearFormErrors(studentForm);

    const id = document.getElementById('studentId').value;
    const name = document.getElementById('studentName').value.trim();
    const regNo = document.getElementById('registerNumber').value.trim();
    const phone = document.getElementById('phoneNumber').value.trim();
    const email = document.getElementById('email').value.trim();

    let isValid = true;

    if (!name) {
      showFieldError('studentName', 'Student Name is required');
      isValid = false;
    }

    if (!regNo) {
      showFieldError('registerNumber', 'Register Number is required');
      isValid = false;
    } else {
      // Check Uniqueness
      const existing = students.find(s => s.registerNumber.toLowerCase() === regNo.toLowerCase() && s.id !== parseInt(id || '0'));
      if (existing) {
        showFieldError('registerNumber', 'Register Number must be unique');
        isValid = false;
      }
    }

    // Phone Validation: Exactly 10 digits
    const phoneRegex = /^\d{10}$/;
    if (!phone) {
      showFieldError('phoneNumber', 'Phone Number is required');
      isValid = false;
    } else if (!phoneRegex.test(phone)) {
      showFieldError('phoneNumber', 'Phone Number must contain exactly 10 digits');
      isValid = false;
    }

    // Email Validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!email) {
      showFieldError('email', 'Email Address is required');
      isValid = false;
    } else if (!emailRegex.test(email)) {
      showFieldError('email', 'Please enter a valid email address');
      isValid = false;
    }

    if (!isValid) return;

    if (id) {
      // Edit existing
      const index = students.findIndex(s => s.id === parseInt(id));
      if (index !== -1) {
        const oldReg = students[index].registerNumber;
        students[index] = { id: parseInt(id), studentName: name, registerNumber: regNo, phoneNumber: phone, email: email };
        
        // Update cascade in events if register number changed
        events.forEach(ev => {
          if (ev.studentRegisterNumber === oldReg) {
            ev.studentRegisterNumber = regNo;
            ev.studentName = name;
          }
        });

        showToast('Student updated successfully!', 'success');
      }
    } else {
      // Add new
      const newId = students.length > 0 ? Math.max(...students.map(s => s.id)) + 1 : 1;
      students.push({ id: newId, studentName: name, registerNumber: regNo, phoneNumber: phone, email: email });
      showToast('Student added successfully!', 'success');
    }

    saveState();
    closeModal(studentModal);
  });

  window.openAddStudentModal = function() {
    studentForm.reset();
    document.getElementById('studentId').value = '';
    document.getElementById('studentModalTitle').textContent = 'Add New Student';
    clearFormErrors(studentForm);
    openModal(studentModal);
  };

  window.openEditStudentModal = function(id) {
    const s = students.find(item => item.id === id);
    if (!s) return;
    studentForm.reset();
    document.getElementById('studentId').value = s.id;
    document.getElementById('studentName').value = s.studentName;
    document.getElementById('registerNumber').value = s.registerNumber;
    document.getElementById('phoneNumber').value = s.phoneNumber;
    document.getElementById('email').value = s.email;
    document.getElementById('studentModalTitle').textContent = 'Edit Student Details';
    clearFormErrors(studentForm);
    openModal(studentModal);
  };

  window.confirmDeleteStudent = function(id) {
    currentDeleteTarget = { type: 'student', id: id };
    document.getElementById('deleteConfirmText').textContent = 'Are you sure you want to delete this student record? This action will also delete related event participations.';
    openModal(deleteModal);
  };

  // =========================================================================
  // Event Module Operations
  // =========================================================================
  function renderEventsTable(dataList) {
    if (dataList.length === 0) {
      eventsTableBody.innerHTML = `
        <tr>
          <td colspan="8" class="empty-state">
            <div class="empty-state-icon">🏆</div>
            <p>No event records found</p>
          </td>
        </tr>
      `;
      return;
    }

    eventsTableBody.innerHTML = dataList.map((e, idx) => `
      <tr>
        <td><strong>#${idx + 1}</strong></td>
        <td>${escapeHtml(e.eventName)}</td>
        <td>${escapeHtml(e.studentName)}</td>
        <td><code style="background:#eff6ff; padding:2px 6px; border-radius:4px; color:#1d4ed8; font-weight:600">${escapeHtml(e.studentRegisterNumber)}</code></td>
        <td>${escapeHtml(e.eventDate)}</td>
        <td>${escapeHtml(e.eventPlace)}</td>
        <td>${getPrizeBadgeHtml(e.prizeWon)}</td>
        <td>
          <div class="action-btns">
            <button class="btn-icon edit" onclick="openEditEventModal(${e.id})" title="Edit Event">✏️</button>
            <button class="btn-icon delete" onclick="confirmDeleteEvent(${e.id})" title="Delete Event">🗑️</button>
          </div>
        </td>
      </tr>
    `).join('');
  }

  function getPrizeBadgeHtml(prize) {
    switch (prize) {
      case '1st Prize':
        return `<span class="badge badge-gold">🥇 1st Prize</span>`;
      case '2nd Prize':
        return `<span class="badge badge-silver">🥈 2nd Prize</span>`;
      case '3rd Prize':
        return `<span class="badge badge-bronze">🥉 3rd Prize</span>`;
      case 'Participation':
      default:
        return `<span class="badge badge-participation">📜 Participation</span>`;
    }
  }

  // Search Events
  searchEventInput.addEventListener('input', (ev) => {
    const q = ev.target.value.toLowerCase().trim();
    const filtered = events.filter(e =>
      e.eventName.toLowerCase().includes(q) ||
      e.studentName.toLowerCase().includes(q) ||
      e.studentRegisterNumber.toLowerCase().includes(q) ||
      e.eventPlace.toLowerCase().includes(q) ||
      e.prizeWon.toLowerCase().includes(q)
    );
    renderEventsTable(filtered);
  });

  // Event Form Submission
  eventForm.addEventListener('submit', (e) => {
    e.preventDefault();
    clearFormErrors(eventForm);

    const id = document.getElementById('eventId').value;
    const sReg = document.getElementById('eventStudentReg').value;
    const sName = document.getElementById('eventStudentName').value.trim();
    const eName = document.getElementById('eventName').value.trim();
    const eDate = document.getElementById('eventDate').value;
    const ePlace = document.getElementById('eventPlace').value.trim();
    const prize = document.getElementById('prizeWon').value;

    let isValid = true;

    if (!sReg) {
      showFieldError('eventStudentReg', 'Please select a student register number');
      isValid = false;
    }
    if (!eName) {
      showFieldError('eventName', 'Event Name is required');
      isValid = false;
    }
    if (!eDate) {
      showFieldError('eventDate', 'Event Date is required');
      isValid = false;
    }
    if (!ePlace) {
      showFieldError('eventPlace', 'Event Place is required');
      isValid = false;
    }
    
    // Strict Prize Validation
    const allowedPrizes = ['1st Prize', '2nd Prize', '3rd Prize', 'Participation'];
    if (!prize || !allowedPrizes.includes(prize)) {
      showFieldError('prizeWon', 'Prize Won must be selected from options only');
      isValid = false;
    }

    if (!isValid) return;

    if (id) {
      // Edit
      const index = events.findIndex(item => item.id === parseInt(id));
      if (index !== -1) {
        events[index] = { id: parseInt(id), studentName: sName, studentRegisterNumber: sReg, eventName: eName, eventDate: eDate, eventPlace: ePlace, prizeWon: prize };
        showToast('Event details updated successfully!', 'success');
      }
    } else {
      // Add
      const newId = events.length > 0 ? Math.max(...events.map(item => item.id)) + 1 : 1;
      events.push({ id: newId, studentName: sName, studentRegisterNumber: sReg, eventName: eName, eventDate: eDate, eventPlace: ePlace, prizeWon: prize });
      showToast('Event participation recorded successfully!', 'success');
    }

    saveState();
    closeModal(eventModal);
  });

  window.openAddEventModal = function() {
    eventForm.reset();
    document.getElementById('eventId').value = '';
    document.getElementById('eventModalTitle').textContent = 'Add Event Record';
    clearFormErrors(eventForm);
    openModal(eventModal);
  };

  window.openEditEventModal = function(id) {
    const e = events.find(item => item.id === id);
    if (!e) return;
    eventForm.reset();
    document.getElementById('eventId').value = e.id;
    document.getElementById('eventStudentReg').value = e.studentRegisterNumber;
    document.getElementById('eventStudentName').value = e.studentName;
    document.getElementById('eventName').value = e.eventName;
    document.getElementById('eventDate').value = e.eventDate;
    document.getElementById('eventPlace').value = e.eventPlace;
    document.getElementById('prizeWon').value = e.prizeWon;
    document.getElementById('eventModalTitle').textContent = 'Update Event Details';
    clearFormErrors(eventForm);
    openModal(eventModal);
  };

  window.confirmDeleteEvent = function(id) {
    currentDeleteTarget = { type: 'event', id: id };
    document.getElementById('deleteConfirmText').textContent = 'Are you sure you want to delete this event participation record?';
    openModal(deleteModal);
  };

  // Perform Deletion on Confirmation
  document.getElementById('confirmDeleteBtn').addEventListener('click', () => {
    if (!currentDeleteTarget) return;

    if (currentDeleteTarget.type === 'student') {
      const studentToDelete = students.find(s => s.id === currentDeleteTarget.id);
      students = students.filter(s => s.id !== currentDeleteTarget.id);
      if (studentToDelete) {
        // Cascade delete events for this student
        events = events.filter(e => e.studentRegisterNumber !== studentToDelete.registerNumber);
      }
      showToast('Student and associated events deleted.', 'success');
    } else if (currentDeleteTarget.type === 'event') {
      events = events.filter(e => e.id !== currentDeleteTarget.id);
      showToast('Event participation record deleted.', 'success');
    }

    currentDeleteTarget = null;
    saveState();
    closeModal(deleteModal);
  });

  // =========================================================================
  // Modal & Toast Helpers
  // =========================================================================
  window.openModal = function(modalEl) {
    modalEl.classList.add('active');
  };

  window.closeModal = function(modalEl) {
    modalEl.classList.remove('active');
  };

  document.querySelectorAll('.close-btn, .close-modal-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const modal = e.target.closest('.modal-overlay');
      if (modal) closeModal(modal);
    });
  });

  function showFieldError(fieldId, msg) {
    const input = document.getElementById(fieldId);
    if (!input) return;
    const group = input.closest('.form-group');
    if (group) {
      group.classList.add('has-error');
      const errEl = group.querySelector('.error-message');
      if (errEl) errEl.textContent = msg;
    }
  }

  function clearFormErrors(formEl) {
    formEl.querySelectorAll('.form-group').forEach(group => {
      group.classList.remove('has-error');
    });
  }

  function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    const icon = type === 'success' ? '✅' : '⚠️';
    toast.innerHTML = `<span>${icon}</span><span>${escapeHtml(message)}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateX(100%)';
      toast.style.transition = 'all 0.3s ease-out';
      setTimeout(() => toast.remove(), 300);
    }, 3500);
  }

  function escapeHtml(str) {
    if (!str) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  // Backend Sync Optional Check
  async function fetchStatsFromBackend() {
    try {
      const res = await fetch('/api/stats');
      if (res.ok) {
        const data = await res.json();
        console.log('Connected to Java JDBC MySQL Backend:', data);
        document.getElementById('dbBackendBadge').innerHTML = '<span class="status-dot"></span> Java JDBC MySQL Connected';
      }
    } catch (e) {
      // Backend not running, standard browser standalone mode
    }
  }

  // Initial Load Call
  renderAll();
});
