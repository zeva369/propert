import { AfterViewInit, Component, OnDestroy } from '@angular/core';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements AfterViewInit, OnDestroy{
  private resizeListener!: () => void;

  ngAfterViewInit(): void {
    const burgerCheckbox = document.querySelector<HTMLInputElement>('.burguer-menu');

    if (burgerCheckbox) {
      // Define el listener y guárdalo para poder eliminarlo después
      this.resizeListener = () => {
        if (window.innerWidth > 768) { // Ajusta según el breakpoint de tu diseño
          burgerCheckbox.checked = false; // Desmarca el checkbox para cerrar el menú
        }
      };

      // Agrega el listener al evento resize
      window.addEventListener('resize', this.resizeListener);
    }
  }

  ngOnDestroy(): void {
    // Elimina el listener al destruir el componente para evitar fugas de memoria
    if (this.resizeListener) {
      window.removeEventListener('resize', this.resizeListener);
    }
  }
  
  closeMenu(): void {
    const burgerCheckbox = document.querySelector<HTMLInputElement>('.burguer-menu');
    if (burgerCheckbox) {
      burgerCheckbox.checked = false;
    }
  }
}
