<html>
<body>
<h1>libalary manager api(json)</h1>
<a>host: https://librarymanagerapimain.herokuapp.com</a>
<h2>member api</h2>
<div>
    <div>/member/login
    request:
    {"username":"","password":""}
    </div>
    <div>/member/logout
    request: 
    {"session":""}
    </div>
    <div>/member/register
    request: 
    {"fullname":"","email":"","username":"","password":""}
    </div>
</div>
<h2>book api</h2>
<div>
    <div>/book/add_books
    request: 
    {title:"",limit:-1} -1:all
    </div>
    <div>/book/update_books
    request:
    {"session": "589e07cd-1f69-4cfd-a5d6-f5537fd22c85","book":{"page_number": 100,"release_date": "2022-11-01","author": "","id": 2,"title": "doraemon","type": "thiếu nhi","image": "base64"}}
    </div>
    <div>/book/delete_books
    request:
    "session":"dcd76af8-3aea-4614-a563-f9953e46b982","book":{"id":3}
    </div>
    <div>/book/get_books
    request: 
    {"session":"589e07cd-1f69-4cfd-a5d6-f5537fd22c85","book":{"title":"doraemon","type":"thiếu nhi","author":"","release_date":"2022-11-01","page_number":100,"image":"anh"}}
    </div>
</div>
</body>
</html>
