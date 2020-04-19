const STATUS_STYLES = {
  'UNKNOWN': 'badge badge-secondary',
  'AVAILABLE': 'badge badge-success',
  'UNAVAILABLE': 'badge badge-warning',
};

const renderForm = (service) => {
  const serviceFormHeader = document.querySelector('#service-form h1');
  const serviceFormUrlInput = document.querySelector('#service-form #url');
  const serviceFormNameInput = document.querySelector('#service-form #name');
  serviceFormUrlInput.value = service ? service.url : '';
  serviceFormNameInput.value = service ? service.name : '';
  serviceFormHeader.innerText = service ? 'Edit service' : 'Add new service';
  const saveButton = document.querySelector('#submit-service-form');
  saveButton.onclick = () => {
    let name= document.querySelector('#name').value;
    let url = document.querySelector('#url').value;
    service ? onUpdate(service.id, name, url) : onCreate(name, url);
    document.querySelector('#name').value = '';
    document.querySelector('#url').value = '';
  }
}

const onCreate = (name, url) => {
  fetch('http://localhost:8080/service', {
    method: 'post',
    headers: {
    'Accept': 'application/json, text/plain, */*',
    'Content-Type': 'application/json'
    },
    body: JSON.stringify({url, name})
   }).then((res)=> {
     if (res.ok) {
       displayNotification('Created service sucessfully', 'success');
       renderServiceList();
     } else {
      displayNotification('Error saving service', 'error')
     }
   });
};


const onUpdate = (id, name, url) => {
  fetch(`http://localhost:8080/service/${id}`, {
    method: 'put',
    headers: {
    'Accept': 'application/json, text/plain, */*',
    'Content-Type': 'application/json'
    },
    body: JSON.stringify({url, name})
   }).then((res)=> {
    if (res.ok) {
      displayNotification('Updated service sucessfully', 'success');
      renderServiceList();
    } else {
      displayNotification('Error saving service', 'error')
    }
    renderForm();
   });
};


const onDelete = (id) => {
    fetch(`http://localhost:8080/service/${id}`, {
      method: 'delete',
      headers: {
      'Accept': 'application/json, text/plain, */*',
      },
    }).then((res)=> {
      if (res.ok) {
        displayNotification('Deleted service sucessfully', 'success');
        renderServiceList();
      } else {
        displayNotification('Error deleting service', 'error')
      }
    });
}

const renderServiceList = () => {
  const listContainer = document.querySelector('#service-list');
  listContainer.innerHTML = "";
  let servicesRequest = new Request('http://localhost:8080/service');
  fetch(servicesRequest)
  .then(function(response) { return response.json(); })
  .then(function(serviceList) {
    serviceList.forEach(service => {
      listContainer.appendChild(renderService(service));;
    });
  });
};

const renderService = (service) => {
  const li = document.createElement("li");
  li.className = 'list-group-item';
  li.setAttribute('x-service-url', service.url);
  li.appendChild(document.createTextNode(service.name));

  const status = document.createElement("span");
  status.className = STATUS_STYLES[service.status];
  status.innerText = service.status;
  li.appendChild(status);

  const deleteButton = document.createElement("button");
  deleteButton.className = 'btn btn-danger';
  deleteButton.innerText = 'Delete';
  deleteButton.onclick = () => onDelete(service.id);
  li.appendChild(deleteButton);

  const editButton = document.createElement("button");
  editButton.className = 'btn btn-light';
  editButton.innerText = 'Edit'; 
  editButton.onclick = () => renderForm(service);
  li.appendChild(editButton);
  return li;
};

document.addEventListener('DOMContentLoaded', () => {
  renderServiceList();
  setTimeout(renderServiceList, 6000);
  renderForm();
})


const displayNotification = (text, type) => {
  console.log('getting invoked');
  const notificationContainer = document.querySelector('#notification-container');
  const notification = document.createElement("div");
  const style = type === 'success' ? 'btn btn-success' : 'btn btn-warning';
  notification.className = `notification fade ${style}`;
  notification.innerText = text;
  notificationContainer.appendChild(notification);
  setTimeout(() => {
    console.log('making visible');
    notification.className = `notification ${style}`;
  }, 0);
  setTimeout(() => {
    console.log('making invisible');
    notification.className = `notification fade ${style}`;
  }, 3500);
  setTimeout(() => {
    console.log('deleting');
    notification.className = `notification`;
    notificationContainer.removeChild(notification);
  }, 5000);
}