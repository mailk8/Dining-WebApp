package de.marcel.restaurant.ejb;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "SEQUENCE")
public class SEQUENCE implements Serializable
{
	private static final long serialVersionUID = 1L;


	@Id
	@Column(name="SEQ_NAME", nullable = false, columnDefinition = "VARCHAR(50)")
	private String SEQ_NAME;

	@Column(name="SEQ_COUNT", nullable = true, columnDefinition = "INT(19)" )
	private long SEQ_COUNT;


}


