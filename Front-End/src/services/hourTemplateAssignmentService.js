import apiClient from './apiConfig.js';

class HourTemplateAssignmentService {
  // Get all assignments
  async getAllAssignments() {
    try {
      const response = await apiClient.get('/api/schedules/hour-template-assignments');
      if (response.data) {
        return response;
      }
    } catch {
      console.log('Using local assignments (backend not available)');
    }
    
    // Use local storage
    const stored = localStorage.getItem('hourTemplateAssignments');
    if (stored) {
      return { data: JSON.parse(stored) };
    }
    return { data: [] };
  }

  // Get assignments by month and year
  async getAssignmentsByPeriod(month, year) {
    const assignments = await this.getAllAssignments();
    const filtered = assignments.data.filter(a => 
      a.month === month && a.year === year
    );
    return { data: filtered };
  }

  // Get assignments by employee
  async getAssignmentsByEmployee(employeeId) {
    const assignments = await this.getAllAssignments();
    const filtered = assignments.data.filter(a => 
      a.employeeId === employeeId
    );
    return { data: filtered };
  }

  // Get assignments by location
  async getAssignmentsByLocation(locationId) {
    const assignments = await this.getAllAssignments();
    const filtered = assignments.data.filter(a => 
      a.locationId === locationId
    );
    return { data: filtered };
  }

  // Create assignment
  async createAssignment(assignmentData) {
    const assignments = await this.getAllAssignments();
    
    // Check if assignment already exists for this employee/location/month/year
    const exists = assignments.data.find(a => 
      a.employeeId === assignmentData.employeeId &&
      a.locationId === assignmentData.locationId &&
      a.month === assignmentData.month &&
      a.year === assignmentData.year
    );
    
    if (exists) {
      throw new Error('Ya existe una asignación para este empleado, ubicación y período');
    }
    
    // Generate ID
    const newAssignment = {
      ...assignmentData,
      id: Date.now(),
      createdAt: new Date().toISOString()
    };
    
    const updatedAssignments = [...assignments.data, newAssignment];
    
    try {
      await apiClient.post('/api/schedules/hour-template-assignments', newAssignment);
    } catch {
      console.log('Saving to local storage (backend not available)');
    }
    
    localStorage.setItem('hourTemplateAssignments', JSON.stringify(updatedAssignments));
    return { data: newAssignment };
  }

  // Update assignment
  async updateAssignment(id, assignmentData) {
    const assignments = await this.getAllAssignments();
    const updatedAssignments = assignments.data.map(a => 
      a.id === id ? { ...a, ...assignmentData, updatedAt: new Date().toISOString() } : a
    );
    
    try {
      await apiClient.put(`/api/schedules/hour-template-assignments/${id}`, assignmentData);
    } catch {
      console.log('Saving to local storage (backend not available)');
    }
    
    localStorage.setItem('hourTemplateAssignments', JSON.stringify(updatedAssignments));
    return { data: updatedAssignments.find(a => a.id === id) };
  }

  // Delete assignment
  async deleteAssignment(id) {
    const assignments = await this.getAllAssignments();
    const updatedAssignments = assignments.data.filter(a => a.id !== id);
    
    try {
      await apiClient.delete(`/api/schedules/hour-template-assignments/${id}`);
    } catch {
      console.log('Saving to local storage (backend not available)');
    }
    
    localStorage.setItem('hourTemplateAssignments', JSON.stringify(updatedAssignments));
    return { data: { success: true } };
  }

  // Bulk create assignments
  async bulkCreateAssignments(assignmentsData) {
    const assignments = await this.getAllAssignments();
    const newAssignments = assignmentsData.map(a => ({
      ...a,
      id: Date.now() + Math.random(),
      createdAt: new Date().toISOString()
    }));
    
    const updatedAssignments = [...assignments.data, ...newAssignments];
    
    try {
      await apiClient.post('/api/schedules/hour-template-assignments/bulk', newAssignments);
    } catch {
      console.log('Saving to local storage (backend not available)');
    }
    
    localStorage.setItem('hourTemplateAssignments', JSON.stringify(updatedAssignments));
    return { data: newAssignments };
  }
}

const hourTemplateAssignmentService = new HourTemplateAssignmentService();
export default hourTemplateAssignmentService;
