package com.bubblesessence.ventas.productoingrediente;

import com.bubblesessence.maestros.ingrediente.Ingrediente;
import com.bubblesessence.ventas.producto.Producto;
import jakarta.persistence.*;
import lombok.*;

/**
 * Mapea grp_ven.tbl_ven_productoingrediente: la "receta" de cada jabón,
 * es decir, qué ingredientes lleva y en qué cantidad referencial.
 */
@Entity
@Table(
        name = "tbl_ven_productoingrediente",
        schema = "grp_ven",
        uniqueConstraints = @UniqueConstraint(
                name = "ak_productoingrediente",
                columnNames = {"cpnid_producto", "cpnid_ingrediente"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoIngrediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cpnid_productoingrediente")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cpnid_producto", nullable = false,
            foreignKey = @ForeignKey(name = "fk_productoingrediente_producto"))
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cpnid_ingrediente", nullable = false,
            foreignKey = @ForeignKey(name = "fk_productoingrediente_ingrediente"))
    private Ingrediente ingrediente;

    @Column(name = "cpccantidadreferencial", length = 30)
    private String cantidadReferencial;
}
