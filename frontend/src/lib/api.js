const courseResponseDtoToCourse = (dto) => {
    return{
        id: dto.id,
        title: dto.title,
        color: dto.color
    };
};

const documentResponseDtoToDocument = (dto) => {
    return {
        id: dto.id,
        courseId: dto.courseId,
        fileName: dto.fileName,
        fileType: dto.fileType,
        status: dto.status
    }
}

export const createCourse = async(req) => {
    const res = await fetch("/api/v1/courses", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(req)
    });

    if(!res.ok) {
        let message;
        try {
            const errorRes = await res.json();
            message = errorRes.error;
        } catch {
            message = `Request error, is backend running?: ${res.status} ${res.statusText}`
        }
        throw new Error(message);
    }

    return courseResponseDtoToCourse(await res.json());
};

export const listCourses = async() => {
    const res = await fetch("/api/v1/courses", {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
        }
    });

    if(!res.ok) {
        let message;
        try {
            const errorRes = await res.json();
            message = errorRes.error;
        } catch {
            message = `Request error, is backend running?: ${res.status} ${res.statusText}`
        }
        throw new Error(message);
    }
    const dtos = await res.json();
    return dtos.map((dto) => courseResponseDtoToCourse(dto));
};

export const updateCourse = async(
    id, req
) => {
    const res = await fetch(`/api/v1/courses/${id}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(req)
    });
    if(!res.ok) {
        let message;
        try {
            const errorRes = await res.json();
            message = errorRes.error;
        } catch {
            message = `Request error, is backend running?: ${res.status} ${res.statusText}`
        }
        throw new Error(message);
    }
    return courseResponseDtoToCourse(await res.json());
}

export const deleteCourse = async(
    id
) => {
    const res = await fetch(`/api/v1/courses/${id}`, {
        method: "DELETE",
        headers: {
            "Content-Type": "application/json",
        }
    });
    if(!res.ok) {
        let message;
        try {
            const errorRes = await res.json();
            message = errorRes.error;
        } catch {
            message = `Request error, is backend running?: ${res.status} ${res.statusText}`
        }
        throw new Error(message);
    }
}

export const createDocument = async (req) => {
    const res = await fetch('api/v1/documents', {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(req)
    });

    if(!res.ok) {
        let message;
        try {
            const errorRes = await res.json();
            message = errorRes.error;
        } catch {
            message = `Request error, is backend running?: ${res.status} ${res.statusText}`
        }
        throw new Error(message);
    }

    return documentResponseDtoToDocument(await res.json());
}

export const listDocuments = async() => {
    const res = await fetch("/api/v1/documents", {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
        }
    });

    if(!res.ok) {
        let message;
        try {
            const errorRes = await res.json();
            message = errorRes.error;
        } catch {
            message = `Request error, is backend running?: ${res.status} ${res.statusText}`
        }
        throw new Error(message);
    }
    const dtos = await res.json();
    return dtos.map((dto) => documentResponseDtoToDocument(dto));
}

export const deleteDocument = async(id) => {
    const res = await fetch(`/api/v1/documents/${id}`, {
        method: "DELETE",
        headers: {
            "Content-Type": "application/json",
        }
    });
    if(!res.ok) {
        let message;
        try {
            const errorRes = await res.json();
            message = errorRes.error;
        } catch {
            message = `Request error, is backend running?: ${res.status} ${res.statusText}`
        }
        throw new Error(message);
    }
}