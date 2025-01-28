export class User {
    id: string = '';
    name: string = '';
    isGuest: boolean = true;

    constructor(id: string, name: string, isGuest: boolean) {
        this.id = id;
        this.name = name;
        this.isGuest = isGuest;
    }
}